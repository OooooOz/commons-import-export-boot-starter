package com.eximport.export.shared.model;

import java.io.Serializable;
import java.util.Map;

/**
 * 导出任务公共字段基类。
 */
public class ExportTaskFields implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务名称。
     */
    private String taskName;

    /**
     * 业务类型。
     */
    private String businessType;

    /**
     * 业务系统。
     */
    private String businessSystem;

    /**
     * 文件名。
     */
    private String fileName;

    /**
     * sheet 名称。
     */
    private String sheetName;

    /**
     * 操作人。
     */
    private String creator;

    /**
     * 额外业务参数。
     */
    private Map<String, Object> extMap;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessSystem() {
        return businessSystem;
    }

    public void setBusinessSystem(String businessSystem) {
        this.businessSystem = businessSystem;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Map<String, Object> getExtMap() {
        return extMap;
    }

    public void setExtMap(Map<String, Object> extMap) {
        this.extMap = extMap;
    }
}

