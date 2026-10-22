package com.eximport.shared.exporting.support;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.eximport.shared.exporting.excel.ExcelWriterUtils;
import com.eximport.shared.exporting.excel.handler.ExcelPageExportHandler;
import com.eximport.shared.exporting.model.BatchData;

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

    private final ExcelWriterUtils excelWriterUtils;

    public PagedExcelWriteSupport() {
        this(new ExcelWriterUtils());
    }

    public PagedExcelWriteSupport(ExcelWriterUtils excelWriterUtils) {
        if (excelWriterUtils == null) {
            throw new IllegalArgumentException("largeExcelWriter不能为空");
        }
        this.excelWriterUtils = excelWriterUtils;
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
        excelWriterUtils.write(
                tempFile,
                sheetName,
                headClass,
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
        excelWriterUtils.write(
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
