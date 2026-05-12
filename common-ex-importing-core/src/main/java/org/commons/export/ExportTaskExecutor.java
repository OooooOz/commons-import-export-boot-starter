package org.commons.export;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eximport.export.shared.excel.PagedExcelWriteSupport;
import com.eximport.export.shared.support.ExportExecutionSupport;
import com.eximport.export.shared.support.ExportHandlerRegistrySupport;
import com.eximport.export.shared.support.ExportPayloadCopySupport;
import com.eximport.export.shared.support.ExportStorageSupport;
import lombok.extern.slf4j.Slf4j;
import org.commons.domain.mapper.ExportTaskProcessMapper;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.enums.ExportTaskStatusEnum;
import org.commons.export.config.ExportAsyncProperties;
import org.commons.export.config.ExportStorageProperties;
import org.commons.export.handler.ExportTaskHandler;
import org.commons.export.notify.ExportTaskNotifier;
import org.commons.export.storage.ExportFileStorage;
import org.commons.export.storage.StorageResult;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RejectedExecutionException;

/**
 * 异步导出执行器：更新任务状态、分页生成 Excel、上传存储、通知前端。
 */
@Slf4j
@Component
public class ExportTaskExecutor {
    private static final String EXPORT_REJECTED_MESSAGE = "导出任务过多，请稍后重试";

    private final ExportTaskProcessMapper taskMapper;
    private final ExportFileStorage fileStorage;
    private final ExportTaskNotifier notifier;
    private final ExportAsyncProperties asyncProperties;
    private final ExportStorageProperties storageProperties;
    private final PagedExcelWriteSupport excelWriteSupport = new PagedExcelWriteSupport();
    private final Map<String, ExportTaskHandler<?>> handlerMap;
    private ThreadPoolTaskExecutor executor;

    public ExportTaskExecutor(ExportTaskProcessMapper taskMapper,
                              ExportFileStorage fileStorage,
                              ExportTaskNotifier notifier,
                              ExportAsyncProperties asyncProperties,
                              ExportStorageProperties storageProperties,
                              List<ExportTaskHandler<?>> handlers) {
        this.taskMapper = taskMapper;
        this.fileStorage = fileStorage;
        this.notifier = notifier;
        this.asyncProperties = asyncProperties;
        this.storageProperties = storageProperties;
        handlerMap = ExportHandlerRegistrySupport.buildHandlerMap(handlers);
    }

    @PostConstruct
    public void init() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("excel-export-");
        taskExecutor.setCorePoolSize(asyncProperties.getCorePoolSize());
        taskExecutor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        taskExecutor.setQueueCapacity(asyncProperties.getQueueCapacity());
        RejectedExecutionHandler rejectedExecutionHandler = ExportExecutionSupport.resolveRejectedExecutionHandler(asyncProperties.getRejectionPolicy());
        taskExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
        taskExecutor.initialize();
        executor = taskExecutor;
        log.info("导出任务线程池初始化完成，handlers={}, corePoolSize={}, maxPoolSize={}, queueCapacity={}, rejectionPolicy={}",
                handlerMap.keySet(), asyncProperties.getCorePoolSize(), asyncProperties.getMaxPoolSize(),
                asyncProperties.getQueueCapacity(), ExportExecutionSupport.normalizeRejectionPolicy(asyncProperties.getRejectionPolicy()));
    }

    @PreDestroy
    public void destroy() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public void submit(Long taskId, ExportTaskDTO dto) {
        Executor target = executor;
        if (target == null) {
            throw new IllegalStateException("导出线程池未初始化");
        }
        ExportTaskDTO safeDto = snapshotDto(dto);
        try {
            target.execute(() -> runTask(taskId, safeDto));
        } catch (RejectedExecutionException e) {
            log.warn("导出线程池已满，拒绝接收任务，taskId={}", taskId, e);
            markFailure(taskId, EXPORT_REJECTED_MESSAGE);
        }
    }

    private void runTask(Long taskId, ExportTaskDTO dto) {
        ExportTaskProcess task = taskMapper.selectById(taskId);
        if (task == null) {
            log.warn("导出任务不存在，taskId={}", taskId);
            return;
        }
        boolean locked = markProcessing(taskId);
        if (!locked) {
            log.warn("导出任务状态不是初始态，跳过执行，taskId={}", taskId);
            return;
        }

        File tempFile = null;
        try {
            String handlerKey = ExportHandlerRegistrySupport.buildHandlerKey(task.getBusinessSystem(), task.getBusinessType());
            ExportTaskHandler<?> handler = handlerMap.get(handlerKey);
            if (handler == null) {
                throw new IllegalArgumentException("未找到导出处理器，businessSystem=" + task.getBusinessSystem() + ", businessType=" + task.getBusinessType());
            }

            String fileName = ExportExecutionSupport.normalizeFileName(StringUtils.hasText(task.getFileName()) ? task.getFileName() : handler.fileName(dto));
            tempFile = File.createTempFile("export-" + task.getTaskNo() + "-", ".xlsx");
            excelWriteSupport.writeExcel(
                    tempFile,
                    dto,
                    handler,
                    asyncProperties.getPageSize(),
                    asyncProperties.getMaxRowsPerSheet(),
                    asyncProperties.getMaxQueryPages());
            String objectName = ExportStorageSupport.buildObjectName(storageProperties.getObjectPrefix(), task.getTaskNo(), fileName);
            StorageResult storageResult = fileStorage.upload(tempFile, objectName, ExportStorageSupport.XLSX_CONTENT_TYPE);

            ExportTaskProcess update = new ExportTaskProcess();
            update.setId(taskId);
            update.setStatus(ExportTaskStatusEnum.SUCCESS.getCode());
            update.setFileName(fileName);
            update.setFileUrl(storageResult.getUrl());
            update.setMessage("导出完成");
            update.setEndTime(new Date());
            LambdaUpdateWrapper<ExportTaskProcess> wrapper = new LambdaUpdateWrapper<ExportTaskProcess>()
                    .eq(ExportTaskProcess::getId, taskId)
                    .eq(ExportTaskProcess::getStatus, ExportTaskStatusEnum.PROCESSING.getCode());
            taskMapper.update(update, wrapper);
            ExportTaskProcess latest = taskMapper.selectById(taskId);
            notifier.notify(latest);
        } catch (Exception e) {
            log.error("导出任务执行失败，taskId={}", taskId, e);
            markFailure(taskId, ExportExecutionSupport.limitMessage(e.getMessage()));
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                log.warn("删除导出临时文件失败：{}", tempFile.getAbsolutePath());
            }
        }
    }

    private boolean markProcessing(Long taskId) {
        ExportTaskProcess update = new ExportTaskProcess();
        update.setStatus(ExportTaskStatusEnum.PROCESSING.getCode());
        update.setStartTime(new Date());
        update.setMessage("导出处理中");
        LambdaUpdateWrapper<ExportTaskProcess> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ExportTaskProcess::getId, taskId)
                .eq(ExportTaskProcess::getStatus, ExportTaskStatusEnum.INIT.getCode());
        return taskMapper.update(update, wrapper) > 0;
    }

    private void markFailure(Long taskId, String message) {
        ExportTaskProcess update = new ExportTaskProcess();
        update.setId(taskId);
        update.setStatus(ExportTaskStatusEnum.FAIL.getCode());
        update.setMessage(ExportExecutionSupport.limitMessage(message));
        update.setEndTime(new Date());
        LambdaUpdateWrapper<ExportTaskProcess> wrapper = new LambdaUpdateWrapper<ExportTaskProcess>()
                .eq(ExportTaskProcess::getId, taskId)
                .in(ExportTaskProcess::getStatus,
                        ExportTaskStatusEnum.INIT.getCode(),
                        ExportTaskStatusEnum.PROCESSING.getCode());
        taskMapper.update(update, wrapper);
        notifier.notify(taskMapper.selectById(taskId));
    }

    private ExportTaskDTO snapshotDto(ExportTaskDTO dto) {
        if (dto == null) {
            return new ExportTaskDTO();
        }
        ExportTaskDTO snapshot = new ExportTaskDTO();
        snapshot.setTaskName(dto.getTaskName());
        snapshot.setBusinessType(dto.getBusinessType());
        snapshot.setBusinessSystem(dto.getBusinessSystem());
        snapshot.setFileName(dto.getFileName());
        snapshot.setSheetName(dto.getSheetName());
        snapshot.setCreator(dto.getCreator());
        if (dto.getExtMap() != null) {
            snapshot.setExtMap(ExportPayloadCopySupport.copyMap(dto.getExtMap()));
        }
        return snapshot;
    }
}

