package org.commons.exporting.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 异步导出线程池配置。
 */
@Data
@ConfigurationProperties(prefix = "common.export.async")
public class ExportAsyncProperties {
    private int corePoolSize = 4;
    private int maxPoolSize = 8;
    private int queueCapacity = 200;
    private int pageSize = 5000;
    private long maxRowsPerSheet = 1000000L;
}

