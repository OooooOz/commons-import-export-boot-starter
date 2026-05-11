package org.commons.exporting.infrastructure.service;

import org.commons.exporting.domain.model.ExportTaskCreateRequest;
import org.commons.exporting.domain.model.ExportTaskInfo;
import org.commons.exporting.domain.service.Exporting;
import org.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import org.commons.exporting.infrastructure.client.model.ExportTaskDTO;
import org.commons.exporting.infrastructure.client.model.ExportTaskVO;

/**
 * 异步导出 SDK 默认实现：任务元数据存储在 core 服务，业务数据导出逻辑在业务系统本地异步执行。
 */
public class DefaultExporting implements Exporting {

    private final RemoteExportTaskClient remoteExportTaskClient;
    private final StarterAsyncExportExecutor starterAsyncExportExecutor;

    public DefaultExporting(RemoteExportTaskClient remoteExportTaskClient,
                            StarterAsyncExportExecutor starterAsyncExportExecutor) {
        this.remoteExportTaskClient = remoteExportTaskClient;
        this.starterAsyncExportExecutor = starterAsyncExportExecutor;
    }

    @Override
    public ExportTaskInfo createTask(ExportTaskCreateRequest request) {
        ExportTaskCreateRequest safeRequest = request == null ? new ExportTaskCreateRequest() : request;
        ExportTaskVO task = remoteExportTaskClient.createTask(toDto(safeRequest));
        ExportTaskInfo info = toInfo(task);
        safeRequest.setFileName(info.getFileName());
        starterAsyncExportExecutor.submit(info.getId(), safeRequest);
        return info;
    }

    @Override
    public ExportTaskInfo getTask(Long id) {
        return toInfo(remoteExportTaskClient.getTask(id));
    }

    private ExportTaskDTO toDto(ExportTaskCreateRequest request) {
        ExportTaskDTO dto = new ExportTaskDTO();
        if (request == null) {
            return dto;
        }
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

