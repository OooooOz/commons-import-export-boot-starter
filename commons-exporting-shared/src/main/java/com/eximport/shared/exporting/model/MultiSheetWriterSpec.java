package com.eximport.shared.exporting.model;

import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.WriteHandler;
import com.eximport.shared.exporting.excel.loader.MultiSheetDataLoader;

import java.util.Collections;
import java.util.List;

/**
 * 多sheet流式导出的单sheet配置
 */
public class MultiSheetWriterSpec {

    private final String sheetName;
    private final Class<?> headClass;
    private final int pageSize;
    private final MultiSheetDataLoader loader;
    /**
     * 写入处理器列表（SheetWriteHandler/CellWriteHandler等），可为空
     */
    private final List<WriteHandler> handlers;
    /**
     * 表头相对默认位置下移的行数，用于表头上方存在说明行等场景，0 表示不下移
     */
    private final int relativeHeadRowIndex;
    /**
     * 单sheet最大数据行数，超出时自动切分新sheet；&lt;=0 时取默认值（xlsx上限扣除表头下移行数）
     */
    private final long maxRowsPerSheet;

    public MultiSheetWriterSpec(String sheetName, Class<?> headClass, int pageSize, MultiSheetDataLoader loader, CellWriteHandler handler) {
        this(sheetName, headClass, pageSize, loader, handler == null ? null : Collections.singletonList(handler), 0, 0L);
    }

    public MultiSheetWriterSpec(String sheetName, Class<?> headClass, int pageSize, MultiSheetDataLoader loader, List<WriteHandler> handlers, int relativeHeadRowIndex) {
        this(sheetName, headClass, pageSize, loader, handlers, relativeHeadRowIndex, 0L);
    }

    public MultiSheetWriterSpec(String sheetName, Class<?> headClass, int pageSize, MultiSheetDataLoader loader, List<WriteHandler> handlers, int relativeHeadRowIndex, long maxRowsPerSheet) {
        this.sheetName = sheetName;
        this.headClass = headClass;
        this.pageSize = pageSize;
        this.loader = loader;
        this.handlers = handlers;
        this.relativeHeadRowIndex = relativeHeadRowIndex;
        this.maxRowsPerSheet = maxRowsPerSheet;
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

    public List<WriteHandler> getHandlers() {
        return handlers;
    }

    public int getRelativeHeadRowIndex() {
        return relativeHeadRowIndex;
    }

    public long getMaxRowsPerSheet() {
        return maxRowsPerSheet;
    }
}
