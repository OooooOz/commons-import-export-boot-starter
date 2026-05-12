package com.commons.exporting.infrastructure.service;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.commons.exporting.configure.ExportAsyncProperties;
import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import com.commons.exporting.infrastructure.context.AsyncExportContextPropagator;
import com.commons.exporting.infrastructure.context.AsyncExportContextSnapshot;
import com.commons.exporting.infrastructure.excel.LargeExcelWriter;
import com.commons.exporting.infrastructure.file.CoreExportGeneratedFileUploader;
import com.commons.exporting.infrastructure.file.ExportGeneratedFileUploader;
import com.commons.exporting.infrastructure.handle.AsyncExportHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 业务系统本地异步导出执行器。
 * <p>
 * 该执行器负责在本地线程池中调度导出任务、匹配导出处理器、生成 Excel 临时文件，
 * 并将执行状态及文件上传结果统一回写到 core 服务。
 */
@Slf4j
public class StarterAsyncExportExecutor implements InitializingBean, DisposableBean {
    private final RemoteExportTaskClient remoteExportTaskClient;
    private final ExportAsyncProperties asyncProperties;
    private final AsyncExportContextPropagator asyncExportContextPropagator;
    private final ExportGeneratedFileUploader exportGeneratedFileUploader;
    private final LargeExcelWriter excelWriter = new LargeExcelWriter();
    private final Map<String, AsyncExportHandler<?>> handlerMap;
    private ThreadPoolTaskExecutor executor;

    public StarterAsyncExportExecutor(RemoteExportTaskClient remoteExportTaskClient,
                                      ExportAsyncProperties asyncProperties,
                                      AsyncExportContextPropagator asyncExportContextPropagator,
                                      List<AsyncExportHandler<?>> handlers) {
        this(remoteExportTaskClient, asyncProperties, asyncExportContextPropagator, new CoreExportGeneratedFileUploader(remoteExportTaskClient), handlers);
    }

    public StarterAsyncExportExecutor(RemoteExportTaskClient remoteExportTaskClient,
                                      ExportAsyncProperties asyncProperties,
                                      AsyncExportContextPropagator asyncExportContextPropagator,
                                      ExportGeneratedFileUploader exportGeneratedFileUploader,
                                      List<AsyncExportHandler<?>> handlers) {
        this.remoteExportTaskClient = remoteExportTaskClient;
        this.asyncProperties = asyncProperties;
        this.asyncExportContextPropagator = asyncExportContextPropagator;
        this.exportGeneratedFileUploader = exportGeneratedFileUploader;
        this.handlerMap = buildHandlerMap(handlers);
    }

    @Override
    public void afterPropertiesSet() {
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

    @Override
    public void destroy() {
        if (executor != null) executor.shutdown();
    }

    /**
     * 提交导出任务到本地线程池异步执行。
     *
     * @param taskId core 侧导出任务主键
     * @param request 导出任务上下文
     */
    public void submit(Long taskId, ExportTaskCreateRequest request) {
        Executor target = executor;
        if (target == null) throw new IllegalStateException("导出线程池未初始化");
        ExportTaskCreateRequest safeRequest = snapshotRequest(request);
        AsyncExportContextSnapshot snapshot = asyncExportContextPropagator.capture();
        long submitThreadId = Thread.currentThread().getId();
        target.execute(() -> runTaskWithContext(snapshot, submitThreadId, taskId, safeRequest));
    }

    private void runTaskWithContext(AsyncExportContextSnapshot snapshot, long submitThreadId, Long taskId, ExportTaskCreateRequest request) {
        if (Thread.currentThread().getId() == submitThreadId) {
            runTask(taskId, request);
            return;
        }
        AsyncExportContextSnapshot safeSnapshot = snapshot == null ? AsyncExportContextSnapshot.noop() : snapshot;
        safeSnapshot.restore();
        try {
            runTask(taskId, request);
        } finally {
            safeSnapshot.clear();
        }
    }

    private void runTask(Long taskId, ExportTaskCreateRequest request) {
        File tempFile = null;
        try {
            remoteExportTaskClient.markProcessing(taskId);
            AsyncExportHandler<?> handler = handlerMap.get(buildHandlerKey(request.getBusinessSystem(), request.getBusinessType()));
            if (handler == null)
                throw new IllegalArgumentException("未找到导出处理器，businessSystem=" + request.getBusinessSystem() + ", businessType=" + request.getBusinessType());

            String fileName = normalizeFileName(StringUtils.hasText(request.getFileName()) ? request.getFileName() : handler.fileName(request));
            tempFile = File.createTempFile("starter-export-" + safeTaskId(taskId) + "-", ".xlsx");
            writeExcel(tempFile, request, handler);
            exportGeneratedFileUploader.upload(taskId, request, tempFile, fileName);
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

    private ExportTaskCreateRequest snapshotRequest(ExportTaskCreateRequest request) {
        if (request == null) return new ExportTaskCreateRequest();
        ExportTaskCreateRequest snapshot = new ExportTaskCreateRequest();
        snapshot.setTaskName(request.getTaskName());
        snapshot.setBusinessType(request.getBusinessType());
        snapshot.setBusinessSystem(request.getBusinessSystem());
        snapshot.setFileName(request.getFileName());
        snapshot.setSheetName(request.getSheetName());
        snapshot.setCreator(request.getCreator());
        if (request.getExtMap() != null) {
            snapshot.setExtMap(new LinkedHashMap<String, Object>(request.getExtMap()));
        }
        return snapshot;
    }
}

