package com.eximport.export.shared.excel;

import java.util.List;

/**
 * 大文件 Excel 分页数据加载器契约（从 {@link LargeExcelWriter} 内部接口抽离）。
 *
 * <p>实现方按页号加载数据，配合 {@link LargeExcelWriter#write} 逐页写入，
 * 避免一次性把全部数据放入内存。</p>
 *
 * @param <T> Excel 行模型类型
 */
public interface PageDataLoader<T> {

    /**
     * 分页加载数据。
     *
     * @param pageNo   页号，从 1 开始
     * @param pageSize 单页条数
     * @return 当前页数据；返回空集合或 null 表示数据已加载完毕
     */
    List<T> load(long pageNo, int pageSize);

    /**
     * 加载器期望的单页查询条数。
     * <p>
     * 返回大于 0 的值时优先生效，覆盖调用方传入的 pageSize（通常来自配置）；
     * 返回小于等于 0 时以调用方传入的 pageSize 为准。
     * <p>
     * 当业务实现内部固定了分页大小时，必须声明一致值，
     * 否则分页终止条件（返回条数不足一页即结束）会误判最后一页导致丢数据。
     */
    default int pageSize() {
        return 0;
    }
}
