package org.commons.importing.configure;


import org.commons.exporting.configure.ExportAsyncProperties;
import org.commons.exporting.domain.service.Exporting;
import org.commons.exporting.infrastructure.client.ExportTaskCoreFeignClient;
import org.commons.exporting.infrastructure.client.RemoteExportTaskClient;
import org.commons.exporting.infrastructure.handle.AsyncExportHandler;
import org.commons.exporting.infrastructure.service.DefaultExporting;
import org.commons.exporting.infrastructure.service.StarterAsyncExportExecutor;
import org.commons.importing.Importing;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableFeignClients(basePackageClasses = ExportTaskCoreFeignClient.class)
@EnableConfigurationProperties({ExportAsyncProperties.class})
public class DefaultImportingAutoConfiguration {
    public DefaultImportingAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(Importing.class)
    public Importing importing() {
        return new DefaultImporting();
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteExportTaskClient remoteExportTaskClient(ExportTaskCoreFeignClient exportTaskCoreFeignClient) {
        return new RemoteExportTaskClient(exportTaskCoreFeignClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public StarterAsyncExportExecutor starterAsyncExportExecutor(RemoteExportTaskClient remoteExportTaskClient,
                                                                 ExportAsyncProperties exportAsyncProperties,
                                                                 ObjectProvider<AsyncExportHandler<?>> handlersProvider) {
        List<AsyncExportHandler<?>> handlers = handlersProvider.orderedStream().collect(Collectors.toList());
        return new StarterAsyncExportExecutor(remoteExportTaskClient, exportAsyncProperties, handlers);
    }

    @Bean
    @ConditionalOnMissingBean
    public Exporting exporting(RemoteExportTaskClient remoteExportTaskClient,
                               StarterAsyncExportExecutor starterAsyncExportExecutor) {
        return new DefaultExporting(remoteExportTaskClient, starterAsyncExportExecutor);
    }
}
