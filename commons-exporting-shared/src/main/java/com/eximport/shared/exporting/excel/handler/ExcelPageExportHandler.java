package com.eximport.shared.exporting.excel.handler;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;

import java.util.List;

/**
 * 通用分页 Excel 导出处理器契约。
 *
 * @param <R> 导出请求上下文类型
 * @param <T> Excel 行模型类型
 */
public interface ExcelPageExportHandler<R, T> {
    /**
     * Excel 表头模型类型。
     */
    Class<T> headClass();

    /**
     * sheet 名称。
     */
    default String sheetName(R request) {
        return "数据";
    }

    /**
     * 自定义 EasyExcel 写入构建器。
     */
    default void customizeWriter(R request, ExcelWriterBuilder writerBuilder) {
        // 默认无扩展
    }

    /**
     * 分页查询导出数据。
     */
    List<T> queryPage(R request, long pageNo, int pageSize);

    /**
     * 当前处理器期望的单页查询条数。
     * <p>
     * 返回大于 0 的值时优先生效，覆盖调用方传入的配置 pageSize；
     * 返回小于等于 0 时回退为调用方传入的配置值（如 common.export.async.pageSize）。
     * <p>
     * 当业务实现内部固定了分页大小时，必须覆写该方法返回一致值，
     * 否则分页终止条件（返回条数不足一页即结束）会误判最后一页导致丢数据。
     */
    default int pageSize() {
        return 0;
    }
}

