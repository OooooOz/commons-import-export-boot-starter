package org.commons.export.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 异步导出线程池配置。
 */
@Data
@ConfigurationProperties(prefix = "common.export.async")
public class ExportAsyncProperties {
    /** 核心线程数，建议按 CPU/数据库承载能力设置。 */
    private int corePoolSize = 4;
    /** 最大线程数。 */
    private int maxPoolSize = 8;
    /** 等待队列容量，过大可能导致任务堆积。 */
    private int queueCapacity = 200;
    /** 每页查询/写入数量。 */
    private int pageSize = 5000;
    /** 单 sheet 最大数据行数，Excel xlsx 上限为 1048576 行，需预留表头。 */
    private long maxRowsPerSheet = 1000000L;
    /** 最大分页查询次数，防止业务分页异常时无限循环。 */
    private long maxQueryPages = 10000L;
}

