package com.commons.exporting.infrastructure.file;

import com.commons.exporting.domain.model.ExportTaskCreateRequest;

import java.io.File;

/**
 * 业务系统导出文件上传器。
 * <p>
 * 当上传模式配置为 {@link GeneratedFileUploadMode#BUSINESS} 时，业务系统需要实现该接口，
 * 将本地生成的导出文件上传到自身 OSS / MinIO / 对象存储，并返回最终可访问的文件地址。
 */
public interface BusinessExportFileUploader {
    /**
     * 上传业务系统本地生成的导出文件，并返回最终文件访问地址。
     *
     * @param request 导出任务请求上下文
     * @param file 本地临时导出文件
     * @param fileName 最终导出文件名
     * @return 外部可访问的文件地址，不能为空
     */
    String upload(ExportTaskCreateRequest request, File file, String fileName);
}

