package com.eximport.export.shared.excel;

import java.util.List;

/**
 * 流式导出单批数据。
 * <p>
 * 与 sdk 模块的 {@code com.commons.exporting.infrastructure.model.BatchData} 契约一致，
 * 供 shared 模块的 {@link LargeExcelWriter} 分页调度使用。
 *
 * @param <T> 导出行类型
 */
public class BatchData<T> {

    /**
     * 本批行数据
     */
    private final List<T> rows;

    /**
     * 总页数；已知时回填，不足整页或页码已达总页数时流式导出结束，未知为0
     */
    private final long totalPages;

    public BatchData(List<T> rows, long totalPages) {
        this.rows = rows;
        this.totalPages = totalPages;
    }

    /**
     * 总页数未知的批次数据
     */
    public static <T> BatchData<T> of(List<T> rows) {
        return new BatchData<>(rows, 0L);
    }

    /**
     * 带总页数的批次数据
     */
    public static <T> BatchData<T> of(List<T> rows, long totalPages) {
        return new BatchData<>(rows, totalPages);
    }

    public List<T> getRows() {
        return rows;
    }

    public long getTotalPages() {
        return totalPages;
    }
}
