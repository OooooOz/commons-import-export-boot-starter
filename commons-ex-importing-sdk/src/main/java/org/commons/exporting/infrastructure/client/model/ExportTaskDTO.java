package org.commons.exporting.infrastructure.client.model;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * SDK 发送给 core 服务的导出任务创建请求。
 */
@Data
public class ExportTaskDTO {
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

