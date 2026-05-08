package org.commons.domain.model.dto;

import lombok.Data;

/**
 * 导出任务分页查询条件。
 */
@Data
public class ExportTaskPageQuery {
    private String businessSystem;
    private String businessType;
    private String creator;
    private Integer status;
    private String orderByClause;
}
