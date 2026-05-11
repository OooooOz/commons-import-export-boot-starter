package com.commons.exporting.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 异步导出执行配置。
 * <p>
 * 该配置用于控制业务系统本地导出线程池行为，以及 Excel 分页写入的批次大小。
 */
@Data
@ConfigurationProperties(prefix = "common.export.async")
public class ExportAsyncProperties {
    /**
     * 线程池核心线程数。
     */
    private int corePoolSize = 4;

    /**
     * 线程池最大线程数。
     */
    private int maxPoolSize = 8;

    /**
     * 任务队列容量。
     */
    private int queueCapacity = 200;

    /**
     * 业务数据分页查询大小。
     */
    private int pageSize = 5000;

    /**
     * 单个 sheet 允许写入的最大行数。
     */
    private long maxRowsPerSheet = 1000000L;
}

