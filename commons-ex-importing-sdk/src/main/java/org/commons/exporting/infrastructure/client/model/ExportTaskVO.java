package org.commons.exporting.infrastructure.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

/**
 * core 服务返回的导出任务信息。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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

