package org.commons.export.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 导出模块配置入口。
 */
@Configuration
@EnableConfigurationProperties({ExportAsyncProperties.class, ExportStorageProperties.class})
public class ExportAutoConfiguration {
}

