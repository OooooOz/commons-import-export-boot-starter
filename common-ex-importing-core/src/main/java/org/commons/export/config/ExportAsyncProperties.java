package org.commons.export.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 异步导出线程池配置。
 */
@ConfigurationProperties(prefix = "common.export.async")
public class ExportAsyncProperties extends com.eximport.shared.exporting.config.ExportAsyncProperties {
}

