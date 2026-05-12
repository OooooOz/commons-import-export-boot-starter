package com.eximport.export.shared.support;

/**
 * 具备业务系统和业务类型标识的导出处理器描述契约。
 */
public interface ExportHandlerDescriptor {
    /**
     * 业务系统标识。
     */
    String businessSystem();

    /**
     * 业务类型标识。
     */
    String businessType();
}

