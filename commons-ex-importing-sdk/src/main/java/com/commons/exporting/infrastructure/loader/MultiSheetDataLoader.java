package com.commons.exporting.infrastructure.loader;

import com.commons.exporting.infrastructure.model.BatchData;

/**
 * 多sheet流式导出的分批数据加载器
 *
 * <p>行数据为空或不足单批条数时，该sheet流式导出结束；已知总页数时随批返回，整页结尾提前终止。
 * 加载器可自行以合适粒度累积数据：如下层查询粒度与导出行粒度不一致时，可在内部循环累积到接近单批条数再返回。</p>
 */
@FunctionalInterface
public interface MultiSheetDataLoader {

    /**
     * 加载指定页码的数据
     *
     * @param pageNum 页码（从1开始）
     * @return 批次数据；行数据为空或不足单批条数时，该sheet流式导出结束
     */
    BatchData<?> load(int pageNum);
}
