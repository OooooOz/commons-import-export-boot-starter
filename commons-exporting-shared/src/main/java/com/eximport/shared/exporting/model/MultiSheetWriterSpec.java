package com.eximport.shared.exporting.model;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.eximport.shared.exporting.excel.loader.MultiSheetDataLoader;

/**
 * 多sheet流式导出的单sheet配置
 */
public class MultiSheetWriterSpec {

    private final String sheetName;
    private final Class<?> headClass;
    private final int pageSize;
    private final MultiSheetDataLoader loader;
    private final CellWriteHandler handler;

    public MultiSheetWriterSpec(String sheetName, Class<?> headClass, int pageSize,
                                MultiSheetDataLoader loader, CellWriteHandler handler) {
        this.sheetName = sheetName;
        this.headClass = headClass;
        this.pageSize = pageSize;
        this.loader = loader;
        this.handler = handler;
    }

    public String getSheetName() {
        return sheetName;
    }

    public Class<?> getHeadClass() {
        return headClass;
    }

    public int getPageSize() {
        return pageSize;
    }

    public MultiSheetDataLoader getLoader() {
        return loader;
    }

    public CellWriteHandler getHandler() {
        return handler;
    }
}
