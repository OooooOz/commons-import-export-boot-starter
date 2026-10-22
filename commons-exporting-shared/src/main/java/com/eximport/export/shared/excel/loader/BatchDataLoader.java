package com.eximport.export.shared.excel.loader;

import com.eximport.export.shared.excel.LargeExcelWriter;

/**
 * 分批数据加载器。
 * <p>
 * 与 sdk 模块的 {@code com.commons.exporting.infrastructure.loader.BatchDataLoader} 契约一致，
 * 供 shared 模块的 {@link LargeExcelWriter} 分页调度使用。
 *
 * @param <T> 导出行类型
 */
@FunctionalInterface
public interface BatchDataLoader<T> {

    /**
     * 加载指定页码的数据。
     *
     * @param pageNum 页码（从1开始）
     * @return 批次数据（行数据为空或不足单批条数时，流式导出将结束）；已知总页数时随批返回，
     * 用于整页结尾提前终止，避免多一次空探测
     */
    BatchData<T> load(int pageNum);
}
