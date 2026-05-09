package org.commons.exporting.domain.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * starter 暴露给业务系统的导出任务创建模型。
 */
@Data
public class ExportTaskCreateRequest {
    private String taskNo;
    private String taskName;
    private String businessType;
    private String businessSystem;
    private String fileName;
    private String sheetName;
    private String fileUrl;
    private Date startTime;
    private Date endTime;
    private String creator;
    private Map<String, Object> extMap;
}

