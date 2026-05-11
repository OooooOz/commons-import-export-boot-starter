package org.commons.exporting.infrastructure.service;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.commons.exporting.configure.ExportAsyncProperties;
import org.commons.exporting.domain.model.ExportTaskCreateRequest;
import org.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import org.commons.exporting.infrastructure.excel.LargeExcelWriter;
import org.commons.exporting.infrastructure.handle.AsyncExportHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 业务系统本地执行导出逻辑，任务状态和文件统一回写到 core 服务。
 */
@Slf4j
public class StarterAsyncExportExecutor {
    private final RemoteExportTaskClient remoteExportTaskClient;
    private final ExportAsyncProperties asyncProperties;
    private final LargeExcelWriter excelWriter = new LargeExcelWriter();
    private final Map<String, AsyncExportHandler<?>> handlerMap;
    private ThreadPoolTaskExecutor executor;

    public StarterAsyncExportExecutor(RemoteExportTaskClient remoteExportTaskClient,
                                      ExportAsyncProperties asyncProperties,
                                      List<AsyncExportHandler<?>> handlers) {
        this.remoteExportTaskClient = remoteExportTaskClient;
        this.asyncProperties = asyncProperties;
        this.handlerMap = buildHandlerMap(handlers);
    }

    @PostConstruct
    public void init() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("starter-excel-export-");
        taskExecutor.setCorePoolSize(asyncProperties.getCorePoolSize());
        taskExecutor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        taskExecutor.setQueueCapacity(asyncProperties.getQueueCapacity());
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        executor = taskExecutor;
        log.info("starter导出任务线程池初始化完成，handlers={}", handlerMap.keySet());
    }

    @PreDestroy
    public void destroy() {
        if (executor != null) executor.shutdown();
    }

    public void submit(Long taskId, ExportTaskCreateRequest request) {
        Executor target = executor;
        if (target == null) throw new IllegalStateException("导出线程池未初始化");
        target.execute(() -> runTask(taskId, request));
    }

    private void runTask(Long taskId, ExportTaskCreateRequest request) {
        File tempFile = null;
        try {
            remoteExportTaskClient.markProcessing(taskId);
            AsyncExportHandler<?> handler = handlerMap.get(buildHandlerKey(request.getBusinessSystem(), request.getBusinessType()));
            if (handler == null) {
                throw new IllegalArgumentException("未找到导出处理器，businessSystem=" + request.getBusinessSystem() + ", businessType=" + request.getBusinessType());
            }

            String fileName = normalizeFileName(StringUtils.hasText(request.getFileName()) ? request.getFileName() : handler.fileName(request));
            tempFile = File.createTempFile("starter-export-" + safeTaskId(taskId) + "-", ".xlsx");
            writeExcel(tempFile, request, handler);
            remoteExportTaskClient.uploadSuccess(taskId, tempFile, fileName);
        } catch (Exception e) {
            log.error("starter导出任务执行失败，taskId={}", taskId, e);
            try {
                remoteExportTaskClient.markFailure(taskId, limitMessage(e.getMessage()));
            } catch (Exception remoteException) {
                log.error("回写导出任务失败状态到core失败，taskId={}", taskId, remoteException);
            }
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) log.warn("删除导出临时文件失败：{}", tempFile.getAbsolutePath());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void writeExcel(File tempFile, ExportTaskCreateRequest request, AsyncExportHandler handler) {
        excelWriter.write(
                tempFile,
                handler.sheetName(request),
                handler.headClass(),
                (ExcelWriterBuilder writerBuilder) -> handler.customizeWriter(request, writerBuilder),
                (pageNo, pageSize) -> handler.queryPage(request, pageNo, pageSize),
                asyncProperties.getPageSize(),
                asyncProperties.getMaxRowsPerSheet());
    }

    private Map<String, AsyncExportHandler<?>> buildHandlerMap(List<AsyncExportHandler<?>> handlers) {
        if (handlers == null || handlers.isEmpty()) return Collections.emptyMap();
        Map<String, AsyncExportHandler<?>> mappings = new LinkedHashMap<>();
        for (AsyncExportHandler<?> handler : handlers) {
            String key = buildHandlerKey(handler.businessSystem(), handler.businessType());
            AsyncExportHandler<?> existing = mappings.putIfAbsent(key, handler);
            if (existing != null) throw new IllegalStateException("导出处理器重复注册，key=" + key
                    + ", existing=" + existing.getClass().getName()
                    + ", current=" + handler.getClass().getName());
        }
        return Collections.unmodifiableMap(mappings);
    }

    private String buildHandlerKey(String businessSystem, String businessType) {
        return normalizeKeyPart(businessSystem, "businessSystem") + "_" + normalizeKeyPart(businessType, "businessType");
    }

    private String normalizeKeyPart(String value, String fieldName) {
        if (!StringUtils.hasText(value)) throw new IllegalArgumentException(fieldName + "不能为空");
        return value.trim();
    }

    private String normalizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) fileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!fileName.toLowerCase().endsWith(".xlsx")) fileName = fileName + ".xlsx";
        return fileName;
    }

    private String safeTaskId(Long taskId) {
        return taskId == null ? "unknown" : String.valueOf(taskId);
    }

    private String limitMessage(String message) {
        if (!StringUtils.hasText(message)) return "导出失败";
        return message.length() > 250 ? message.substring(0, 250) : message;
    }
}

