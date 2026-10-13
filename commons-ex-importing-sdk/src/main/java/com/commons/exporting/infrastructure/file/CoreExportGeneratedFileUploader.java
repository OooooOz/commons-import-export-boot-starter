package com.commons.exporting.infrastructure.file;

import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.commons.exporting.infrastructure.client.RemoteExportTaskClient;

import java.io.File;

/**
 * core 上传模式下的导出文件上传器。
 * <p>
 * 将本地生成的导出文件直接上传回 core，由 core 负责最终存储与状态维护。
 */
public class CoreExportGeneratedFileUploader implements ExportGeneratedFileUploader {
    private final RemoteExportTaskClient remoteExportTaskClient;

    public CoreExportGeneratedFileUploader(RemoteExportTaskClient remoteExportTaskClient) {
        this.remoteExportTaskClient = remoteExportTaskClient;
    }

    @Override
    public void upload(Long taskId, ExportTaskCreateRequest request, File file, String fileName) {
        remoteExportTaskClient.uploadSuccess(taskId, file, fileName);
    }
}
