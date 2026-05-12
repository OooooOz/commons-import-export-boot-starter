package org.commons.export.config;

import com.eximport.export.shared.config.ExportAsyncPropertiesSupport;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 异步导出线程池配置。
 */
@ConfigurationProperties(prefix = "common.export.async")
public class ExportAsyncProperties extends ExportAsyncPropertiesSupport {
}

