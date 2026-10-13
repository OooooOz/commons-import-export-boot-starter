package com.commons.importing.configure;


import com.commons.importing.Importing;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 导入 starter 自动配置。
 * <p>
 * 负责向 Spring 容器中注册默认的 {@link Importing} 门面实现。
 * <p>
 * 默认开启；仅使用导出能力的服务可通过 {@code common.import.enabled=false} 关闭。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "common.import", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DefaultImportingAutoConfiguration {
    public DefaultImportingAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(Importing.class)
    public Importing importing() {
        return new DefaultImporting();
    }
}
