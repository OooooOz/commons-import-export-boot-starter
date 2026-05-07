package org.commons.domain.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 导出任务返回对象。
 */
@Data
public class ExportTaskVO {
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

