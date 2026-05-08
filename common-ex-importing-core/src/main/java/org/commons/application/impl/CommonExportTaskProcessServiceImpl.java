package org.commons.application.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.commons.adapter.dto.ExportTaskPageParamDTO;
import org.commons.application.CommonExportTaskProcessService;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.dto.ExportTaskPageQuery;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.vo.ExportTaskVO;
import org.commons.domain.model.vo.LocalExportFileDownload;
import org.commons.domain.service.ExportTaskProcessService;
import org.commons.export.notify.ExportTaskNotifier;
import org.commons.export.storage.LocalExportFileStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
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

    public CommonExportTaskProcessServiceImpl(ExportTaskProcessService exportTaskProcessService,
                                              ExportTaskNotifier exportTaskNotifier,
                                              ObjectProvider<LocalExportFileStorage> localExportFileStorageProvider) {
        this.exportTaskProcessService = exportTaskProcessService;
        this.exportTaskNotifier = exportTaskNotifier;
        this.localExportFileStorageProvider = localExportFileStorageProvider;
    }

    @Override
    public ExportTaskVO createTask(ExportTaskDTO dto) {
        return exportTaskProcessService.createTask(dto);
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
        if (localExportFileStorage == null) {
            return LocalExportFileDownload.notFound("当前未启用本地文件存储");
        }
        File file = localExportFileStorage.getFile(objectName);
        if (!file.exists() || !file.isFile()) {
            return LocalExportFileDownload.notFound("文件不存在");
        }
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
        if (sortFields.isEmpty()) {
            return DEFAULT_ORDER_BY;
        }
        List<String> orderDirections = splitCsv(order);
        List<String> orderByItems = new ArrayList<>();
        for (int i = 0; i < sortFields.size(); i++) {
            String column = SORT_FIELD_MAPPING.get(sortFields.get(i));
            if (column == null) {
                continue;
            }
            String direction = normalizeDirection(resolveDirection(orderDirections, i));
            orderByItems.add(column + " " + direction.toUpperCase(Locale.ROOT));
        }
        if (orderByItems.isEmpty()) {
            return DEFAULT_ORDER_BY;
        }
        return String.join(", ", orderByItems);
    }

    private String resolveDirection(List<String> orderDirections, int index) {
        if (orderDirections.isEmpty()) {
            return DESC;
        }
        if (index < orderDirections.size()) {
            return orderDirections.get(index);
        }
        return orderDirections.get(orderDirections.size() - 1);
    }

    private String normalizeDirection(String direction) {
        return ASC.equalsIgnoreCase(direction) ? ASC : DESC;
    }

    private List<String> splitCsv(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(this::trimToNull)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toList());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
