package org.commons.infrastructure.util;

import com.eximport.export.shared.model.ExportTaskFields;
import com.eximport.export.shared.support.ExportTaskFingerprintSupport;

/**
 * 导出请求指纹工具。
 * <p>
 * 用于根据业务侧可见的导出请求参数生成稳定指纹，实现“同一请求不重复执行”。
 */
public final class ExportTaskRequestFingerprintUtil {
    private ExportTaskRequestFingerprintUtil() {
    }

    /**
     * 基于业务侧可见字段构建请求指纹。
     */
    public static String build(ExportTaskFields taskFields) {
        return ExportTaskFingerprintSupport.build(taskFields);
    }
}

