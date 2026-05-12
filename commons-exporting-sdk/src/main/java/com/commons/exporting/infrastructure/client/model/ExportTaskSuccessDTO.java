package com.commons.exporting.infrastructure.client.model;

import lombok.Data;

/**
 * SDK 回写导出成功信息。
 * <p>
 * 当业务系统将文件上传到自身 OSS / MinIO 等外部存储后，可通过该模型仅回写文件名和文件地址。
 */
@Data
public class ExportTaskSuccessDTO {
    /**
     * 导出文件名。
     */
    private String fileName;

    /**
     * 业务系统外部存储返回的文件访问地址。
     */
    private String fileUrl;

    /**
     * 成功提示信息。
     */
    private String message;
}

