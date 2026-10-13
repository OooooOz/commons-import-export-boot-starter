package com.commons.exporting.infrastructure.service;


import com.eximport.export.shared.model.ExportTaskVO;
import com.eximport.export.shared.support.ExportPayloadCopySupport;
import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.commons.exporting.domain.model.ExportTaskInfo;
import com.commons.exporting.domain.service.Exporting;
import com.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import com.commons.exporting.infrastructure.client.model.ExportTaskDTO;

/**
 * {@link Exporting} 的默认实现。
 * <p>
 * 该实现负责衔接 core 服务任务创建能力与业务系统本地异步导出执行能力：
 * 先通过远程客户端创建任务，再将实际导出工作提交给本地执行器处理。
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
        ExportTaskCreateRequest safeRequest = snapshotRequest(request);
        ExportTaskVO task = remoteExportTaskClient.createTask(toDto(safeRequest));
        ExportTaskInfo info = toInfo(task);
        safeRequest.setFileName(info.getFileName());
        if (task == null || task.getSubmitRequired() == null || task.getSubmitRequired())
            starterAsyncExportExecutor.submit(info.getId(), safeRequest);
        return info;
    }

    @Override
    public ExportTaskInfo getTask(Long id) {
        return toInfo(remoteExportTaskClient.getTask(id));
    }

    /**
     * 将 SDK 暴露的创建请求转换为远程调用 DTO。
     */
    private ExportTaskDTO toDto(ExportTaskCreateRequest request) {
        ExportTaskDTO dto = new ExportTaskDTO();
        if (request == null) return dto;
        dto.setTaskName(request.getTaskName());
        dto.setBusinessType(request.getBusinessType());
        dto.setBusinessSystem(request.getBusinessSystem());
        dto.setFileName(request.getFileName());
        dto.setSheetName(request.getSheetName());
        dto.setCreator(request.getCreator());
        dto.setExtMap(request.getExtMap());
        return dto;
    }

    /**
     * 将 core 返回的任务信息转换为 SDK 输出模型。
     */
    private ExportTaskInfo toInfo(ExportTaskVO vo) {
        ExportTaskInfo info = new ExportTaskInfo();
        if (vo == null) return info;
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

    private ExportTaskCreateRequest snapshotRequest(ExportTaskCreateRequest request) {
        if (request == null) {
            return new ExportTaskCreateRequest();
        }
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

