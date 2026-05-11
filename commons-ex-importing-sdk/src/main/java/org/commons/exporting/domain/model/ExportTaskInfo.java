package org.commons.exporting.domain.model;

import lombok.Data;

import java.util.Date;

/**
 * starter 暴露给业务系统的导出任务信息模型。
 */
@Data
public class ExportTaskInfo {
    private Long id;
    private String businessSystem;
    private String businessType;
    private String taskNo;
    private String taskName;
    private Integer status;
    private String fileName;
    private String fileUrl;
    private String message;
    private Date startTime;
    private Date endTime;
    private String creator;
}

