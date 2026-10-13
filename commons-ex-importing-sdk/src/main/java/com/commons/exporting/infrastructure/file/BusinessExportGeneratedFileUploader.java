package com.commons.exporting.infrastructure.file;

import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * 业务侧上传模式下的导出文件上传器。
 * <p>
 * 先调用业务系统自定义上传器将文件上传到本方 OSS / MinIO，再把最终文件地址回写给 core。
 */
public class BusinessExportGeneratedFileUploader implements ExportGeneratedFileUploader {
    private final RemoteExportTaskClient remoteExportTaskClient;
    private final BusinessExportFileUploader businessExportFileUploader;

    public BusinessExportGeneratedFileUploader(RemoteExportTaskClient remoteExportTaskClient,
                                               BusinessExportFileUploader businessExportFileUploader) {
        this.remoteExportTaskClient = remoteExportTaskClient;
        this.businessExportFileUploader = businessExportFileUploader;
    }

    @Override
    public void upload(Long taskId, ExportTaskCreateRequest request, File file, String fileName) {
        String fileUrl = businessExportFileUploader.upload(request, file, fileName);
        if (!StringUtils.hasText(fileUrl)) {
            throw new IllegalStateException("业务系统上传导出文件成功后必须返回可访问的fileUrl");
        }
        remoteExportTaskClient.reportSuccess(taskId, fileName, fileUrl.trim());
    }
}

