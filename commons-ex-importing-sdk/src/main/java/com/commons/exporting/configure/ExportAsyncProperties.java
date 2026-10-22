package com.commons.exporting.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 异步导出执行配置。
 * <p>
 * 该配置用于控制业务系统本地导出线程池行为，以及 Excel 分页写入的批次大小。
 */
@ConfigurationProperties(prefix = "common.export.async")
public class ExportAsyncProperties extends com.eximport.shared.exporting.config.ExportAsyncProperties {
}

