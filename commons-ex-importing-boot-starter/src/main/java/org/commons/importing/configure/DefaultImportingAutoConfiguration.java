package org.commons.importing.configure;


import org.commons.importing.Importing;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "org.commons.application",
        "org.commons.adapter",
        "org.commons.export",
        "org.commons.exporting",
        "org.commons.infrastructure"
})
@MapperScan(basePackages = "org.commons.domain.mapper")
public class DefaultImportingAutoConfiguration {
    public DefaultImportingAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(Importing.class)
    public Importing importing() {
        return new DefaultImporting();
    }
}
