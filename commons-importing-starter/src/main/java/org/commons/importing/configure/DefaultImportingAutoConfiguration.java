package org.commons.importing.configure;


import org.commons.importing.Importing;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 导入 starter 自动配置。
 * <p>
 * 负责向 Spring 容器中注册默认的 {@link Importing} 门面实现。
 */
@AutoConfiguration
public class DefaultImportingAutoConfiguration {
    public DefaultImportingAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(Importing.class)
    public Importing importing() {
        return new DefaultImporting();
    }
}
