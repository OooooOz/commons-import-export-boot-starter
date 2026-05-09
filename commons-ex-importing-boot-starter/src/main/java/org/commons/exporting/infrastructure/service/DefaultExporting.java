package org.commons.exporting.infrastructure.service;

import org.commons.application.CommonExportTaskProcessService;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.vo.ExportTaskVO;
import org.commons.exporting.domain.model.ExportTaskCreateRequest;
import org.commons.exporting.domain.model.ExportTaskInfo;
import org.commons.exporting.domain.service.Exporting;
import org.springframework.stereotype.Service;

/**
 * 异步导出 SDK 默认实现。
 */
@Service
public class DefaultExporting implements Exporting {

    private final CommonExportTaskProcessService commonExportTaskProcessService;

    public DefaultExporting(CommonExportTaskProcessService commonExportTaskProcessService) {
        this.commonExportTaskProcessService = commonExportTaskProcessService;
    }

    @Override
    public ExportTaskInfo createTask(ExportTaskCreateRequest request) {
        return toInfo(commonExportTaskProcessService.createTask(toDto(request)));
    }

    @Override
    public ExportTaskInfo getTask(Long id) {
        return toInfo(commonExportTaskProcessService.getTask(id));
    }

    private ExportTaskDTO toDto(ExportTaskCreateRequest request) {
        ExportTaskDTO dto = new ExportTaskDTO();
        if (request == null) {
            return dto;
        }
        dto.setTaskNo(request.getTaskNo());
        dto.setTaskName(request.getTaskName());
        dto.setBusinessType(request.getBusinessType());
        dto.setBusinessSystem(request.getBusinessSystem());
        dto.setFileName(request.getFileName());
        dto.setSheetName(request.getSheetName());
        dto.setFileUrl(request.getFileUrl());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        dto.setCreator(request.getCreator());
        dto.setExtMap(request.getExtMap());
        return dto;
    }

    private ExportTaskInfo toInfo(ExportTaskVO vo) {
        ExportTaskInfo info = new ExportTaskInfo();
        if (vo == null) {
            return info;
        }
        info.setId(vo.getId());
        info.setBusinessSystem(vo.getBusinessSystem());
        info.setBusinessType(vo.getBusinessType());
        info.setTaskNo(vo.getTaskNo());
        info.setTaskName(vo.getTaskName());
        info.setStatus(vo.getStatus());
        info.setFileName(vo.getFileName());
        info.setFileUrl(vo.getFileUrl());
        info.setMessage(vo.getMessage());
        info.setStartTime(vo.getStartTime());
        info.setEndTime(vo.getEndTime());
        info.setCreator(vo.getCreator());
        return info;
    }
}

