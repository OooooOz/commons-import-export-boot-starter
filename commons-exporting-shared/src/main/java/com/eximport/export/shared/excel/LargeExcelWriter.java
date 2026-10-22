package com.eximport.export.shared.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.eximport.export.shared.exception.ExImportingBusinessException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 大文件 Excel 分页写入工具，避免一次性把全部数据放入内存。
 *
 * <p>分页数据加载契约见 {@link BatchDataLoader}（与 sdk 模块同名接口契约一致），
 * 本类仅负责分页调度与多 sheet 写入。终止规则与
 * {@code CommonExcelUtil#streamWriteExcel} 一致：单批不足 pageSize、空批或已知总页数且页码已达总页数即结束。</p>
 */
public class LargeExcelWriter {

    /**
     * 未显式指定 pageSize 时的默认单页条数，与 ExportAsyncPropertiesSupport 的默认配置保持一致。
     */
    static final int DEFAULT_PAGE_SIZE = 5000;

    /**
     * 单个 sheet 允许的最大数据行数（xlsx 上限 1048576 行，预留 1 行表头）。
     */
    static final long MAX_ROWS_PER_SHEET = 1048575L;

    /**
     * 未显式指定时的默认最大分页查询次数。
     */
    static final long DEFAULT_MAX_QUERY_PAGES = 10000L;

    private static void check(boolean condition, String msg) {
        if (!condition) {
            throw new ExImportingBusinessException(msg);
        }
    }

    public <T> void write(File file,
                          String sheetName,
                          Class<T> headClass,
                          Consumer<ExcelWriterBuilder> writerBuilderCustomizer,
                          List<CellWriteHandler> handlers,
                          BatchDataLoader<T> loader,
                          int pageSize,
                          long maxRowsPerSheet,
                          long maxQueryPages) {
        check(file != null, "file不能为空");
        check(headClass != null, "headClass不能为空");
        check(loader != null, "loader不能为空");
        if (pageSize <= 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (maxRowsPerSheet <= 0 || maxRowsPerSheet > MAX_ROWS_PER_SHEET) {
            maxRowsPerSheet = MAX_ROWS_PER_SHEET;
        }
        if (maxQueryPages <= 0) {
            maxQueryPages = DEFAULT_MAX_QUERY_PAGES;
        }

        ExcelWriterBuilder writerBuilder = EasyExcel.write(file, headClass);
        ExcelWriter writer = this.buildCustomizerExcelWriter(writerBuilder, writerBuilderCustomizer, handlers);
        RuntimeException primary = null;
        try {
            int sheetNo = 0;
            long currentSheetRows = 0L;
            boolean wroteAny = false;
            WriteSheet currentSheet = buildSheet(sheetNo, sheetName);

            for (int pageNum = 1; ; pageNum++) {
                check(pageNum <= maxQueryPages, "导出分页查询超过最大页数限制(" + maxQueryPages + ")，请检查queryPage是否正确使用pageNo分页，并在最后一页返回空集合或不足pageSize的数据");
                BatchData<T> batchData = loader.load(pageNum);
                List<T> batch = batchData == null ? null : batchData.getRows();
                if (batch == null || batch.isEmpty()) {
                    break;
                }
                int size = batch.size();
                check(size <= pageSize, "分页查询返回条数(" + size + ")不能大于pageSize(" + pageSize + ")");

                if (currentSheetRows + size <= maxRowsPerSheet) {
                    // 常规路径：整页装入当前 sheet，无需切分
                    writer.write(batch, currentSheet);
                    currentSheetRows += size;
                } else {
                    // 跨 sheet 路径：按当前 sheet 剩余容量切分写入
                    int offset = 0;
                    while (offset < size) {
                        if (currentSheetRows >= maxRowsPerSheet) {
                            sheetNo++;
                            currentSheetRows = 0L;
                            currentSheet = buildSheet(sheetNo, sheetName);
                        }
                        int canWrite = (int) Math.min((long) size - offset, maxRowsPerSheet - currentSheetRows);
                        writer.write(batch.subList(offset, offset + canWrite), currentSheet);
                        currentSheetRows += canWrite;
                        offset += canWrite;
                    }
                }
                wroteAny = true;

                if (size < pageSize) {
                    break;
                }
                // 已知总页数且当前已是最后一页，无需再多一次空探测
                if (batchData.getTotalPages() > 0 && pageNum >= batchData.getTotalPages()) {
                    break;
                }
            }

            if (!wroteAny) {
                writer.write(Collections.emptyList(), currentSheet);
            }
        } catch (RuntimeException ex) {
            primary = ex;
            throw ex;
        } finally {
            if (primary == null) {
                writer.finish();
            } else {
                // 业务异常优先；finish() 的次要异常挂到 suppressed，避免掩盖根因
                try {
                    writer.finish();
                } catch (RuntimeException finishEx) {
                    primary.addSuppressed(finishEx);
                }
            }
        }
    }

    private ExcelWriter buildCustomizerExcelWriter(ExcelWriterBuilder writerBuilder, Consumer<ExcelWriterBuilder> writerBuilderCustomizer, List<CellWriteHandler> handlers) {
        if (writerBuilderCustomizer != null) {
            writerBuilderCustomizer.accept(writerBuilder);
        }
        // 单元格写入处理器按列表顺序依次注册（跳过 null 元素）
        if (handlers != null) {
            for (CellWriteHandler handler : handlers) {
                if (handler != null) {
                    writerBuilder.registerWriteHandler(handler);
                }
            }
        }
        return writerBuilder.build();
    }

    private WriteSheet buildSheet(int sheetNo, String sheetName) {
        String name = sheetName == null || sheetName.trim().length() == 0 ? "数据" : sheetName.trim();
        // xlsx sheet 名上限 31 字符；多 sheet 时需容纳 "_序号" 后缀，超长截断避免写入失败
        int maxBaseLength = 31 - (sheetNo > 0 ? String.valueOf(sheetNo + 1).length() + 1 : 0);
        if (name.length() > maxBaseLength) {
            name = name.substring(0, maxBaseLength);
        }
        if (sheetNo > 0) {
            name = name + "_" + (sheetNo + 1);
        }
        return EasyExcel.writerSheet(sheetNo, name).build();
    }
}
