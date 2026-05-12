package com.commons.exporting.infrastructure.service;

import com.commons.exporting.configure.ExportAsyncProperties;
import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import com.commons.exporting.infrastructure.context.AsyncExportContextPropagator;
import com.commons.exporting.infrastructure.context.AsyncExportContextSnapshot;
import com.commons.exporting.infrastructure.file.CoreExportGeneratedFileUploader;
import com.commons.exporting.infrastructure.file.ExportGeneratedFileUploader;
import com.commons.exporting.infrastructure.handle.AsyncExportHandler;
import com.eximport.export.shared.excel.PagedExcelWriteSupport;
import com.eximport.export.shared.support.ExportExecutionSupport;
import com.eximport.export.shared.support.ExportHandlerRegistrySupport;
import com.eximport.export.shared.support.ExportPayloadCopySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * 业务系统本地异步导出执行器。
 * <p>
 * 该执行器负责在本地线程池中调度导出任务、匹配导出处理器、生成 Excel 临时文件，
 * 并将执行状态及文件上传结果统一回写到 core 服务。
 */
@Slf4j
public class StarterAsyncExportExecutor implements InitializingBean, DisposableBean {
    private static final String EXPORT_REJECTED_MESSAGE = "导出任务过多，请稍后重试";

    private final RemoteExportTaskClient remoteExportTaskClient;
    private final ExportAsyncProperties asyncProperties;
    private final AsyncExportContextPropagator asyncExportContextPropagator;
    private final ExportGeneratedFileUploader exportGeneratedFileUploader;
    private final PagedExcelWriteSupport excelWriteSupport = new PagedExcelWriteSupport();
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
        this.handlerMap = ExportHandlerRegistrySupport.buildHandlerMap(handlers);
    }

    @Override
    public void afterPropertiesSet() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("starter-excel-export-");
        taskExecutor.setCorePoolSize(asyncProperties.getCorePoolSize());
        taskExecutor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        taskExecutor.setQueueCapacity(asyncProperties.getQueueCapacity());
        RejectedExecutionHandler rejectedExecutionHandler = ExportExecutionSupport.resolveRejectedExecutionHandler(asyncProperties.getRejectionPolicy());
        taskExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
        taskExecutor.initialize();
        executor = taskExecutor;
        log.info("starter导出任务线程池初始化完成，handlers={}, corePoolSize={}, maxPoolSize={}, queueCapacity={}, rejectionPolicy={}",
                handlerMap.keySet(), asyncProperties.getCorePoolSize(), asyncProperties.getMaxPoolSize(),
                asyncProperties.getQueueCapacity(), ExportExecutionSupport.normalizeRejectionPolicy(asyncProperties.getRejectionPolicy()));
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
        try {
            target.execute(() -> runTaskWithContext(snapshot, submitThreadId, taskId, safeRequest));
        } catch (RejectedExecutionException e) {
            log.warn("starter导出线程池已满，拒绝接收任务，taskId={}", taskId, e);
            try {
                remoteExportTaskClient.markFailure(taskId, EXPORT_REJECTED_MESSAGE);
            } catch (Exception remoteException) {
                log.error("导出任务被拒绝后回写失败状态到core失败，taskId={}", taskId, remoteException);
            }
        }
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
            AsyncExportHandler<?> handler = handlerMap.get(ExportHandlerRegistrySupport.buildHandlerKey(request.getBusinessSystem(), request.getBusinessType()));
            if (handler == null)
                throw new IllegalArgumentException("未找到导出处理器，businessSystem=" + request.getBusinessSystem() + ", businessType=" + request.getBusinessType());

            String fileName = ExportExecutionSupport.normalizeFileName(StringUtils.hasText(request.getFileName()) ? request.getFileName() : handler.fileName(request));
            tempFile = File.createTempFile("starter-export-" + safeTaskId(taskId) + "-", ".xlsx");
            excelWriteSupport.writeExcel(
                    tempFile,
                    request,
                    handler,
                    asyncProperties.getPageSize(),
                    asyncProperties.getMaxRowsPerSheet(),
                    asyncProperties.getMaxQueryPages());
            exportGeneratedFileUploader.upload(taskId, request, tempFile, fileName);
        } catch (Exception e) {
            log.error("starter导出任务执行失败，taskId={}", taskId, e);
            try {
                remoteExportTaskClient.markFailure(taskId, ExportExecutionSupport.limitMessage(e.getMessage()));
            } catch (Exception remoteException) {
                log.error("回写导出任务失败状态到core失败，taskId={}", taskId, remoteException);
            }
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) log.warn("删除导出临时文件失败：{}", tempFile.getAbsolutePath());
        }
    }


    private String safeTaskId(Long taskId) {
        return taskId == null ? "unknown" : String.valueOf(taskId);
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
            snapshot.setExtMap(ExportPayloadCopySupport.copyMap(request.getExtMap()));
        }
        return snapshot;
    }
}

