package com.commons.importing.configure;

import com.commons.importing.Importing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultImportingAutoConfiguration {
    public DefaultImportingAutoConfiguration() {}

    @Bean
    public Importing importing() {
        DefaultImporting defaultImporting = new DefaultImporting();
        return defaultImporting;
    }
}
