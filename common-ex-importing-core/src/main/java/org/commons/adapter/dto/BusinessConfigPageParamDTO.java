package org.commons.adapter.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务配置分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessConfigPageParamDTO extends BasePageDTO {
    private String configType;
    private String businessSystem;
    private String businessType;
}

