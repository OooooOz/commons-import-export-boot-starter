package org.commons.adapter.dto;

import lombok.EqualsAndHashCode;
import lombok.Data;

/**
 * 导出任务分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExportTaskPageParamDTO extends BasePageDTO {
    private String businessSystem;
    private String businessType;
    private String creator;
    private Integer status;
}

