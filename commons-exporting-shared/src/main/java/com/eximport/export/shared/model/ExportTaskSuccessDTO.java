package com.eximport.export.shared.model;

/**
 * 回写导出成功信息。
 */
public class ExportTaskSuccessDTO {
    /**
     * 导出文件名。
     */
    private String fileName;

    /**
     * 文件访问地址。
     */
    private String fileUrl;

    /**
     * 成功提示信息。
     */
    private String message;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

