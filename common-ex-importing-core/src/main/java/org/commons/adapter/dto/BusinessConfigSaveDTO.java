package org.commons.adapter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 业务配置保存参数。
 */
@Data
public class BusinessConfigSaveDTO {
    private Long id;

    @NotBlank(message = "配置类型不能为空")
    private String configType;

    @NotBlank(message = "业务系统不能为空")
    private String businessSystem;

    @NotBlank(message = "业务类型不能为空")
    private String businessType;
}

