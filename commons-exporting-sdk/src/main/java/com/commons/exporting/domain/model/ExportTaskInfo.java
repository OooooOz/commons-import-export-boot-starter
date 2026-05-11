package com.commons.exporting.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * 导出任务信息快照。
 * <p>
 * 该模型用于向业务系统返回 core 侧任务状态、文件信息与执行结果。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportTaskInfo {
    /**
     * core 侧任务主键。
     */
    private Long id;

    /**
     * 业务系统标识。
     */
    private String businessSystem;

    /**
     * 业务类型标识。
     */
    private String businessType;

    /**
     * core 自动生成的唯一任务号。
     */
    private String taskNo;

    /**
     * 任务名称。
     */
    private String taskName;

    /**
     * 任务状态码，具体取值由 core 服务定义。
     */
    private Integer status;

    /**
     * 最终导出文件名。
     */
    private String fileName;

    /**
     * 最终导出文件访问地址。
     */
    private String fileUrl;

    /**
     * 任务状态说明或失败原因。
     */
    private String message;

    /**
     * 导出开始时间。
     */
    private Date startTime;

    /**
     * 导出结束时间。
     */
    private Date endTime;

    /**
     * 任务创建人。
     */
    private String creator;
}

