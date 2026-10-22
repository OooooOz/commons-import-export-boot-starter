package com.eximport.shared.exporting.model;

import javax.validation.constraints.NotBlank;

/**
 * 业务系统提交给导出 SDK 的任务创建模型。
 * <p>
 * 业务类型与业务系统为路由导出处理器的必要字段，创建任务时必须非空。
 */
public class ExportTaskCreateRequest extends ExportTaskFields {
    private static final long serialVersionUID = -2507534785800895369L;

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
