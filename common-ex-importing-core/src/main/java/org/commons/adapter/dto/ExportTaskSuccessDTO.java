package org.commons.adapter.dto;

import lombok.Data;

/**
 * starter 回写导出成功信息。
 * <p>
 * 适用于业务系统已将导出文件上传到自身 OSS / MinIO 等外部存储，
 * 仅需将最终文件地址回写给 core 的场景。
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

