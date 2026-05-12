package com.commons.exporting.infrastructure.file;

import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.commons.exporting.infrastructure.client.RemoteExportTaskClient;

import java.io.File;

/**
 * 默认导出文件上传器。
 * <p>
 * 将业务系统本地生成的 Excel 文件上传到 core，由 core 决定使用本地存储还是自身 OSS 存储。
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

