package com.eximport.export.shared.excel;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * 统一的分页 Excel 写入支持类。
 */
public class PagedExcelWriteSupport {
    public interface PageDataSupplier {
        List<?> load(long pageNo, int pageSize);
    }

    private final LargeExcelWriter largeExcelWriter;

    public PagedExcelWriteSupport() {
        this(new LargeExcelWriter());
    }

    public PagedExcelWriteSupport(LargeExcelWriter largeExcelWriter) {
        if (largeExcelWriter == null) {
            throw new IllegalArgumentException("largeExcelWriter不能为空");
        }
        this.largeExcelWriter = largeExcelWriter;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void writeExcel(File tempFile,
                           String sheetName,
                           Class<?> headClass,
                           Consumer<ExcelWriterBuilder> writerBuilderCustomizer,
                           PageDataSupplier loader,
                           int pageSize,
                           long maxRowsPerSheet,
                           long maxQueryPages) {
        writeExcel(tempFile, sheetName, headClass, writerBuilderCustomizer, null, loader, pageSize, maxRowsPerSheet, maxQueryPages);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void writeExcel(File tempFile,
                           String sheetName,
                           Class<?> headClass,
                           Consumer<ExcelWriterBuilder> writerBuilderCustomizer,
                           List<CellWriteHandler> handlers,
                           PageDataSupplier loader,
                           int pageSize,
                           long maxRowsPerSheet,
                           long maxQueryPages) {
        largeExcelWriter.write(
                tempFile,
                sheetName,
                (Class) headClass,
                writerBuilderCustomizer,
                handlers,
                pageNum -> BatchData.of(loader.load(pageNum, pageSize)),
                pageSize,
                maxRowsPerSheet,
                maxQueryPages);
    }

    public <R, T> void writeExcel(File tempFile,
                                  R request,
                                  ExcelPageExportHandler<R, T> handler,
                                  int pageSize,
                                  long maxRowsPerSheet,
                                  long maxQueryPages) {
        writeExcel(tempFile, request, handler, null, pageSize, maxRowsPerSheet, maxQueryPages);
    }

    public <R, T> void writeExcel(File tempFile,
                                  R request,
                                  ExcelPageExportHandler<R, T> handler,
                                  List<CellWriteHandler> handlers,
                                  int pageSize,
                                  long maxRowsPerSheet,
                                  long maxQueryPages) {
        if (handler == null) {
            throw new IllegalArgumentException("handler不能为空");
        }
        // handler 声明的 pageSize 优先；未声明（<=0）时沿用调用方传入的配置值
        int effectivePageSize = handler.pageSize() > 0 ? handler.pageSize() : pageSize;
        largeExcelWriter.write(
                tempFile,
                handler.sheetName(request),
                handler.headClass(),
                writerBuilder -> handler.customizeWriter(request, writerBuilder),
                handlers,
                pageNum -> BatchData.of(handler.queryPage(request, pageNum, effectivePageSize)),
                effectivePageSize,
                maxRowsPerSheet,
                maxQueryPages);
    }
}
