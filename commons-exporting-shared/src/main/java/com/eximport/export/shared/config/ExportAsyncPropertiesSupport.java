package com.eximport.export.shared.config;

/**
 * 异步导出线程池与分页写入的公共配置基类。
 */
public class ExportAsyncPropertiesSupport {
    /** 核心线程数，建议按 CPU/数据库承载能力设置。 */
    private int corePoolSize = 4;
    /** 最大线程数。 */
    private int maxPoolSize = 8;
    /** 等待队列容量，过大可能导致任务堆积。 */
    private int queueCapacity = 200;
    /**
     * 线程池拒绝策略。
     * <p>
     * 支持：abort、caller-runs。
     */
    private String rejectionPolicy = "abort";
    /** 每页查询/写入数量。 */
    private int pageSize = 5000;
    /** 单 sheet 最大数据行数，Excel xlsx 上限为 1048576 行，需预留表头。 */
    private long maxRowsPerSheet = 1000000L;
    /** 最大分页查询次数，防止业务分页异常时无限循环。 */
    private long maxQueryPages = 10000L;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public String getRejectionPolicy() {
        return rejectionPolicy;
    }

    public void setRejectionPolicy(String rejectionPolicy) {
        this.rejectionPolicy = rejectionPolicy;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getMaxRowsPerSheet() {
        return maxRowsPerSheet;
    }

    public void setMaxRowsPerSheet(long maxRowsPerSheet) {
        this.maxRowsPerSheet = maxRowsPerSheet;
    }

    public long getMaxQueryPages() {
        return maxQueryPages;
    }

    public void setMaxQueryPages(long maxQueryPages) {
        this.maxQueryPages = maxQueryPages;
    }
}

