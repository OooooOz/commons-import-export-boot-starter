package org.commons.export;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.commons.domain.mapper.ExportTaskProcessMapper;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.enums.ExportTaskStatusEnum;
import org.commons.export.config.ExportAsyncProperties;
import org.commons.export.config.ExportStorageProperties;
import org.commons.export.excel.LargeExcelWriter;
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
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步导出执行器：更新任务状态、分页生成 Excel、上传存储、通知前端。
 */
@Slf4j
@Component
public class ExportTaskExecutor {
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXPORT_REJECTED_MESSAGE = "导出任务过多，请稍后重试";

    private final ExportTaskProcessMapper taskMapper;
    private final ExportFileStorage fileStorage;
    private final ExportTaskNotifier notifier;
    private final ExportAsyncProperties asyncProperties;
    private final ExportStorageProperties storageProperties;
    private final LargeExcelWriter excelWriter = new LargeExcelWriter();
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
        handlerMap = buildHandlerMap(handlers);
    }

    @PostConstruct
    public void init() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("excel-export-");
        taskExecutor.setCorePoolSize(asyncProperties.getCorePoolSize());
        taskExecutor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        taskExecutor.setQueueCapacity(asyncProperties.getQueueCapacity());
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        taskExecutor.initialize();
        executor = taskExecutor;
        log.info("导出任务线程池初始化完成，handlers={}", handlerMap.keySet());
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
            String handlerKey = buildHandlerKey(task.getBusinessSystem(), task.getBusinessType());
            ExportTaskHandler<?> handler = handlerMap.get(handlerKey);
            if (handler == null) {
                throw new IllegalArgumentException("未找到导出处理器，businessSystem=" + task.getBusinessSystem() + ", businessType=" + task.getBusinessType());
            }

            String fileName = normalizeFileName(StringUtils.hasText(task.getFileName()) ? task.getFileName() : handler.fileName(dto));
            tempFile = File.createTempFile("export-" + task.getTaskNo() + "-", ".xlsx");
            writeExcel(tempFile, dto, handler);
            String objectName = buildObjectName(task.getTaskNo(), fileName);
            StorageResult storageResult = fileStorage.upload(tempFile, objectName, XLSX_CONTENT_TYPE);

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
            markFailure(taskId, limitMessage(e.getMessage()));
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
        update.setMessage(limitMessage(message));
        update.setEndTime(new Date());
        LambdaUpdateWrapper<ExportTaskProcess> wrapper = new LambdaUpdateWrapper<ExportTaskProcess>()
                .eq(ExportTaskProcess::getId, taskId)
                .in(ExportTaskProcess::getStatus,
                        ExportTaskStatusEnum.INIT.getCode(),
                        ExportTaskStatusEnum.PROCESSING.getCode());
        taskMapper.update(update, wrapper);
        notifier.notify(taskMapper.selectById(taskId));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void writeExcel(File tempFile, ExportTaskDTO dto, ExportTaskHandler handler) {
        excelWriter.write(
                tempFile,
                handler.sheetName(dto),
                handler.headClass(),
                writerBuilder -> handler.customizeWriter(dto, writerBuilder),
                (pageNo, pageSize) -> handler.queryPage(dto, pageNo, pageSize),
                asyncProperties.getPageSize(),
                asyncProperties.getMaxRowsPerSheet(),
                asyncProperties.getMaxQueryPages());
    }

    private String buildObjectName(String taskNo, String fileName) {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String prefix = storageProperties.getObjectPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "exports";
        }
        prefix = trimSlash(prefix);
        return prefix + "/" + date + "/" + taskNo + "-" + fileName;
    }

    private String normalizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            fileName = UUID.randomUUID().toString().replace("-", "") + ".xlsx";
        }
        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            fileName = fileName + ".xlsx";
        }
        return fileName;
    }

    private String trimSlash(String value) {
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String limitMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return "导出失败";
        }
        return message.length() > 250 ? message.substring(0, 250) : message;
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
            snapshot.setExtMap(copyMap(dto.getExtMap()));
        }
        return snapshot;
    }

    private LinkedHashMap<String, Object> copyMap(Map<String, Object> source) {
        LinkedHashMap<String, Object> target = new LinkedHashMap<>(source.size());
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            target.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return target;
    }

    private Object deepCopyValue(Object value) {
        if (value == null) return null;
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character || value instanceof Enum) {
            return value;
        }
        if (value instanceof Date) {
            return new Date(((Date) value).getTime());
        }
        if (value instanceof Map) {
            LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                copy.put(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> copy = new ArrayList<>(list.size());
            for (Object item : list) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value instanceof Set) {
            Set<?> set = (Set<?>) value;
            Set<Object> copy = new LinkedHashSet<>(set.size());
            for (Object item : set) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            List<Object> copy = new ArrayList<>(collection.size());
            for (Object item : collection) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object copy = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, deepCopyValue(Array.get(value, i)));
            }
            return copy;
        }
        return value;
    }

    private Map<String, ExportTaskHandler<?>> buildHandlerMap(List<ExportTaskHandler<?>> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, ExportTaskHandler<?>> mappings = new LinkedHashMap<>();
        for (ExportTaskHandler<?> handler : handlers) {
            String key = buildHandlerKey(handler.businessSystem(), handler.businessType());
            ExportTaskHandler<?> existing = mappings.putIfAbsent(key, handler);
            if (existing != null) {
                throw new IllegalStateException("导出处理器重复注册，key=" + key
                        + ", existing=" + existing.getClass().getName()
                        + ", current=" + handler.getClass().getName());
            }
        }
        return Collections.unmodifiableMap(mappings);
    }

    private String buildHandlerKey(String businessSystem, String businessType) {
        return normalizeKeyPart(businessSystem, "businessSystem")
                + "_"
                + normalizeKeyPart(businessType, "businessType");
    }

    private String normalizeKeyPart(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return value.trim();
    }
}

