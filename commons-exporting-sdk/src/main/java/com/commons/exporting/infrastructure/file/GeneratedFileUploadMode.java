package com.commons.exporting.infrastructure.file;

/**
 * 导出生成文件上传模式。
 * <p>
 * CORE：先回传给 core，再由 core 负责最终存储。
 * BUSINESS：由业务系统上传到自己的 OSS / MinIO，再将最终文件地址回写给 core。
 */
public enum GeneratedFileUploadMode {
    /**
     * 由 core 接收文件并负责最终存储。
     */
    CORE,

    /**
     * 由业务系统自行上传并仅回写最终文件地址。
     */
    BUSINESS
}

