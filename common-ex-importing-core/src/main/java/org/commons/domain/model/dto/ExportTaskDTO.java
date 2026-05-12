package org.commons.domain.model.dto;

import com.eximport.export.shared.model.ExportTaskFields;

import javax.validation.constraints.NotBlank;

public class ExportTaskDTO extends ExportTaskFields {
    /**
     * 业务类型
     */
    @Override
    @NotBlank(message = "业务类型不能为空")
    public String getBusinessType() {
        return super.getBusinessType();
    }

    /**
     * 业务系统
     */
    @Override
    @NotBlank(message = "业务系统不能为空")
    public String getBusinessSystem() {
        return super.getBusinessSystem();
    }
}
