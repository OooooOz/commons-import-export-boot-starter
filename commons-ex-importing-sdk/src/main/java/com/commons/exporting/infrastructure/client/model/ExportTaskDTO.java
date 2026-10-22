package com.commons.exporting.infrastructure.client.model;

import com.eximport.shared.exporting.model.ExportTaskFields;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * SDK 发送给 core 服务的导出任务创建请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExportTaskDTO extends ExportTaskFields {
    private static final long serialVersionUID = 2554883457171122040L;
    private String fileUrl;
    private Date startTime;
    private Date endTime;
}

