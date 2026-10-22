package com.eximport.export.shared.excel;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;

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
        largeExcelWriter.write(
                tempFile,
                sheetName,
                (Class) headClass,
                writerBuilderCustomizer,
                (PageDataLoader) loader::load,
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
        if (handler == null) {
            throw new IllegalArgumentException("handler不能为空");
        }
        largeExcelWriter.write(
                tempFile,
                handler.sheetName(request),
                handler.headClass(),
                writerBuilder -> handler.customizeWriter(request, writerBuilder),
                // handler 声明的 pageSize 优先；未声明（<=0）时沿用调用方传入的配置值
                new PageDataLoader<T>() {
                    @Override
                    public List<T> load(long pageNo, int effectivePageSize) {
                        return handler.queryPage(request, pageNo, effectivePageSize);
                    }

                    @Override
                    public int pageSize() {
                        return handler.pageSize();
                    }
                },
                pageSize,
                maxRowsPerSheet,
                maxQueryPages);
    }
}
