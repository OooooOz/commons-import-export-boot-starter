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

    /**
     * 是否需要由当前请求继续触发导出执行。
     * <p>
     * true 表示本次请求新建了任务，需要继续提交执行；
     * false 表示复用了已有任务，无需重复执行。
     */
    private Boolean submitRequired;
}

