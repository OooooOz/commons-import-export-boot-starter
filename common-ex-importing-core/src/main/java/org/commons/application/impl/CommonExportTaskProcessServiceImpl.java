package org.commons.application.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eximport.export.shared.model.ExportTaskVO;
import com.eximport.export.shared.support.ExportExecutionSupport;
import com.eximport.export.shared.support.ExportStorageSupport;
import org.commons.adapter.dto.ExportTaskPageParamDTO;
import org.commons.application.CommonExportTaskProcessService;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.dto.ExportTaskPageQuery;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.enums.ExportTaskStatusEnum;
import org.commons.domain.model.vo.LocalExportFileDownload;
import org.commons.domain.service.ExportTaskProcessService;
import org.commons.export.config.ExportStorageProperties;
import org.commons.export.notify.ExportTaskNotifier;
import org.commons.export.storage.ExportFileStorage;
import org.commons.export.storage.LocalExportFileStorage;
import org.commons.export.storage.StorageResult;
import org.commons.infrastructure.util.ExportTaskNoGenerator;
import org.commons.infrastructure.util.ExportTaskRequestDedupLockManager;
import org.commons.infrastructure.util.ExportTaskRequestFingerprintUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.util.*;

/**
 * 导出任务应用服务，负责聚合控制器所需的导出任务编排逻辑。
 */
@Service
public class CommonExportTaskProcessServiceImpl implements CommonExportTaskProcessService {

    private static final String ASC = "asc";
    private static final String DESC = "desc";
    private static final String DEFAULT_ORDER_BY = "id DESC";
    private static final Map<String, String> SORT_FIELD_MAPPING = buildSortFieldMapping();

    private final ExportTaskProcessService exportTaskProcessService;
    private final ExportTaskNotifier exportTaskNotifier;
    private final ObjectProvider<LocalExportFileStorage> localExportFileStorageProvider;
    private final ExportFileStorage exportFileStorage;
    private final ExportStorageProperties exportStorageProperties;
    private final ExportTaskRequestDedupLockManager requestDedupLockManager;

    public CommonExportTaskProcessServiceImpl(ExportTaskProcessService exportTaskProcessService,
                                              ExportTaskNotifier exportTaskNotifier,
                                               ObjectProvider<LocalExportFileStorage> localExportFileStorageProvider,
                                               ExportFileStorage exportFileStorage,
                                              ExportStorageProperties exportStorageProperties,
                                              ExportTaskRequestDedupLockManager requestDedupLockManager) {
        this.exportTaskProcessService = exportTaskProcessService;
        this.exportTaskNotifier = exportTaskNotifier;
        this.localExportFileStorageProvider = localExportFileStorageProvider;
        this.exportFileStorage = exportFileStorage;
        this.exportStorageProperties = exportStorageProperties;
        this.requestDedupLockManager = requestDedupLockManager;
    }

    @Override
    public ExportTaskVO createTask(ExportTaskDTO dto) {
        return exportTaskProcessService.createTask(dto);
    }

    @Override
    public ExportTaskVO createClientTask(ExportTaskDTO dto) {
        String requestFingerprint = ExportTaskRequestFingerprintUtil.build(dto);
        return requestDedupLockManager.execute(requestFingerprint, () -> {
            ExportTaskProcess existing = exportTaskProcessService.findReusableTask(requestFingerprint);
            if (existing != null) return toVO(existing, false);
            ExportTaskProcess entity = BeanUtil.copyProperties(dto, ExportTaskProcess.class);
            entity.setRequestFingerprint(requestFingerprint);
            entity.setStatus(ExportTaskStatusEnum.INIT.getCode());
            entity.setMessage("任务已创建，等待业务系统导出");
            saveWithGeneratedTaskNo(entity);
            ExportTaskProcess latest = exportTaskProcessService.getById(entity.getId());
            exportTaskNotifier.notify(latest);
            return toVO(latest, true);
        });
    }

    @Override
    public ExportTaskVO markProcessing(Long id) {
        ExportTaskProcess update = new ExportTaskProcess();
        update.setId(id);
        update.setStatus(ExportTaskStatusEnum.PROCESSING.getCode());
        update.setStartTime(new Date());
        update.setMessage("导出处理中");
        LambdaUpdateWrapper<ExportTaskProcess> wrapper = new LambdaUpdateWrapper<ExportTaskProcess>()
                .eq(ExportTaskProcess::getId, id)
                .eq(ExportTaskProcess::getStatus, ExportTaskStatusEnum.INIT.getCode());
        exportTaskProcessService.update(update, wrapper);
        ExportTaskProcess latest = exportTaskProcessService.getById(id);
        exportTaskNotifier.notify(latest);
        return toVO(latest);
    }

    @Override
    public ExportTaskVO uploadSuccess(Long id, File file, String fileName, String message) {
        ExportTaskProcess task = exportTaskProcessService.getById(id);
        if (task == null) throw new IllegalArgumentException("导出任务不存在，id=" + id);
        String normalizedFileName = ExportExecutionSupport.normalizeFileName(fileName);
        String objectName = ExportStorageSupport.buildObjectName(exportStorageProperties.getObjectPrefix(), task.getTaskNo(), normalizedFileName);
        StorageResult storageResult = exportFileStorage.upload(file, objectName, ExportStorageSupport.XLSX_CONTENT_TYPE);

        return finishSuccess(task, normalizedFileName, storageResult.getUrl(), message);
    }

    @Override
    public ExportTaskVO reportSuccess(Long id, String fileName, String fileUrl, String message) {
        ExportTaskProcess task = exportTaskProcessService.getById(id);
        if (task == null) throw new IllegalArgumentException("导出任务不存在，id=" + id);
        if (!StringUtils.hasText(fileUrl)) throw new IllegalArgumentException("fileUrl不能为空");
        String normalizedFileName = ExportExecutionSupport.normalizeFileName(fileName);
        return finishSuccess(task, normalizedFileName, fileUrl.trim(), message);
    }

    private ExportTaskVO finishSuccess(ExportTaskProcess task, String fileName, String fileUrl, String message) {
        ExportTaskProcess update = new ExportTaskProcess();
        update.setId(task.getId());
        update.setStatus(ExportTaskStatusEnum.SUCCESS.getCode());
        update.setFileName(fileName);
        update.setFileUrl(fileUrl);
        update.setMessage(StringUtils.hasText(message) ? message : "导出完成");
        update.setEndTime(new Date());
        LambdaUpdateWrapper<ExportTaskProcess> wrapper = new LambdaUpdateWrapper<ExportTaskProcess>()
                .eq(ExportTaskProcess::getId, task.getId())
                .in(ExportTaskProcess::getStatus,
                        ExportTaskStatusEnum.INIT.getCode(),
                        ExportTaskStatusEnum.PROCESSING.getCode());
        exportTaskProcessService.update(update, wrapper);
        ExportTaskProcess latest = exportTaskProcessService.getById(task.getId());
        exportTaskNotifier.notify(latest);
        return toVO(latest);
    }

    @Override
    public ExportTaskVO markFailure(Long id, String message) {
        ExportTaskProcess update = new ExportTaskProcess();
        update.setId(id);
        update.setStatus(ExportTaskStatusEnum.FAIL.getCode());
        update.setMessage(ExportExecutionSupport.limitMessage(message));
        update.setEndTime(new Date());
        LambdaUpdateWrapper<ExportTaskProcess> wrapper = new LambdaUpdateWrapper<ExportTaskProcess>()
                .eq(ExportTaskProcess::getId, id)
                .in(ExportTaskProcess::getStatus,
                        ExportTaskStatusEnum.INIT.getCode(),
                        ExportTaskStatusEnum.PROCESSING.getCode());
        exportTaskProcessService.update(update, wrapper);
        ExportTaskProcess latest = exportTaskProcessService.getById(id);
        exportTaskNotifier.notify(latest);
        return toVO(latest);
    }

    @Override
    public ExportTaskVO getTask(Long id) {
        return exportTaskProcessService.getTask(id);
    }

    @Override
    public IPage<ExportTaskProcess> page(ExportTaskPageParamDTO query) {
        Page<ExportTaskProcess> page = new Page<>(query.getPage(), query.getSize());
        return exportTaskProcessService.pageQuery(page, toPageQuery(query));
    }

    @Override
    public SseEmitter subscribe(String creator) {
        return exportTaskNotifier.subscribe(creator);
    }

    @Override
    public LocalExportFileDownload loadLocalFile(String objectName) {
        LocalExportFileStorage localExportFileStorage = localExportFileStorageProvider.getIfAvailable();
        if (localExportFileStorage == null) return LocalExportFileDownload.notFound("当前未启用本地文件存储");
        File file = localExportFileStorage.getFile(objectName);
        if (!file.exists() || !file.isFile()) return LocalExportFileDownload.notFound("文件不存在");
        return LocalExportFileDownload.success(file);
    }

    private ExportTaskPageQuery toPageQuery(ExportTaskPageParamDTO query) {
        ExportTaskPageQuery pageQuery = new ExportTaskPageQuery();
        pageQuery.setBusinessSystem(trimToNull(query.getBusinessSystem()));
        pageQuery.setBusinessType(trimToNull(query.getBusinessType()));
        pageQuery.setCreator(trimToNull(query.getCreator()));
        pageQuery.setStatus(query.getStatus());
        pageQuery.setOrderByClause(buildOrderByClause(query.getSort(), query.getOrder()));
        return pageQuery;
    }

    private String buildOrderByClause(String sort, String order) {
        List<String> sortFields = splitCsv(sort);
        if (sortFields.isEmpty()) return DEFAULT_ORDER_BY;
        List<String> orderDirections = splitCsv(order);
        List<String> orderByItems = new ArrayList<>();
        for (int i = 0; i < sortFields.size(); i++) {
            String column = SORT_FIELD_MAPPING.get(sortFields.get(i));
            if (column == null) continue;
            String direction = normalizeDirection(resolveDirection(orderDirections, i));
            orderByItems.add(column + " " + direction.toUpperCase(Locale.ROOT));
        }
        if (orderByItems.isEmpty()) return DEFAULT_ORDER_BY;
        return String.join(", ", orderByItems);
    }

    private String resolveDirection(List<String> orderDirections, int index) {
        if (orderDirections.isEmpty()) return DESC;
        if (index < orderDirections.size()) return orderDirections.get(index);
        return orderDirections.get(orderDirections.size() - 1);
    }

    private String normalizeDirection(String direction) {
        return ASC.equalsIgnoreCase(direction) ? ASC : DESC;
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(this::trimToNull)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ExportTaskVO toVO(ExportTaskProcess entity) {
        return toVO(entity, null);
    }

    private ExportTaskVO toVO(ExportTaskProcess entity, Boolean submitRequired) {
        if (entity == null) return null;
        ExportTaskVO vo = BeanUtil.copyProperties(entity, ExportTaskVO.class);
        vo.setSubmitRequired(submitRequired);
        return vo;
    }

    private void saveWithGeneratedTaskNo(ExportTaskProcess entity) {
        DuplicateKeyException lastException = null;
        for (int i = 0; i < 5; i++) {
            entity.setTaskNo(ExportTaskNoGenerator.generate());
            try {
                exportTaskProcessService.save(entity);
                return;
            } catch (DuplicateKeyException e) {
                lastException = e;
            }
        }
        throw new IllegalStateException("生成唯一导出任务号失败，请稍后重试", lastException);
    }


    private static Map<String, String> buildSortFieldMapping() {
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("id", "id");
        mapping.put("taskNo", "task_no");
        mapping.put("taskName", "task_name");
        mapping.put("businessSystem", "business_system");
        mapping.put("businessType", "business_type");
        mapping.put("status", "status");
        mapping.put("fileName", "file_name");
        mapping.put("fileUrl", "file_url");
        mapping.put("message", "message");
        mapping.put("startTime", "start_time");
        mapping.put("endTime", "end_time");
        mapping.put("creator", "creator");
        return mapping;
    }
}
