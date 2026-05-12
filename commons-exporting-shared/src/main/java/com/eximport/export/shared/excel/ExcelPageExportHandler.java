package com.eximport.export.shared.excel;

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
}

