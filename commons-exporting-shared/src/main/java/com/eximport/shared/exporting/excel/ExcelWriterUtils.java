package com.eximport.shared.exporting.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.eximport.shared.exporting.excel.converter.BigDecimalFormatExcelConverter;
import com.eximport.shared.exporting.excel.loader.BatchDataLoader;
import com.eximport.shared.exporting.exception.ExImportingBusinessException;
import com.eximport.shared.exporting.model.BatchData;

import java.io.File;
import java.io.OutputStream;
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
public class ExcelWriterUtils {

    /**
     * 未显式指定 pageSize 时的默认单页条数，与 ExportAsyncPropertiesSupport 的默认配置保持一致。
     */
    static final int DEFAULT_PAGE_SIZE = 5000;

    /**
     * 单个 sheet 允许的最大数据行数（xlsx 上限 1048576 行，预留 1 行表头）。
     */
    public static final long MAX_ROWS_PER_SHEET = 1048575L;

    /**
     * 未显式指定时的默认最大分页查询次数。
     */
    static final long DEFAULT_MAX_QUERY_PAGES = 10000L;

    private static void check(boolean condition, String msg) {
        if (!condition) {
            throw new ExImportingBusinessException(msg);
        }
    }

    /**
     * 分页流式写入到文件，见 {@link #write(OutputStream, String, Class, Consumer, List, BatchDataLoader, int, long, long)}。
     */
    public <T> void write(File file,
                          String sheetName,
                          Class<?> headClass,
                          Consumer<ExcelWriterBuilder> writerBuilderCustomizer,
                          List<CellWriteHandler> handlers,
                          BatchDataLoader<T> loader,
                          int pageSize,
                          long maxRowsPerSheet,
                          long maxQueryPages) {
        check(file != null, "file不能为空");
        write(fileOutputStream(file), sheetName, headClass, writerBuilderCustomizer, handlers, loader, pageSize, maxRowsPerSheet, maxQueryPages);
    }

    /**
     * 分页流式写入到输出流（如 HTTP 响应流），避免一次性把全部数据放入内存。
     *
     * @param outputStream            目标输出流（调用方负责流的生命周期语义，本方法通过 finish() 落盘）
     * @param sheetName               sheet 名（跨 sheet 切分时自动追加 "_序号" 后缀并截断至 31 字符）
     * @param headClass               表头模型类型
     * @param writerBuilderCustomizer 写入构建器自定义（可空）
     * @param handlers                单元格写入处理器列表（可空；按列表顺序依次注册，跳过 null 元素）
     * @param loader                  分批数据加载器
     * @param pageSize                单批条数（&lt;=0 时取默认 5000）
     * @param maxRowsPerSheet         单 sheet 最大行数（&lt;=0 或超上限时取 xlsx 上限 1048575）
     * @param maxQueryPages           最大分页查询次数（&lt;=0 时取默认 10000）
     */
    public <T> void write(OutputStream outputStream,
                          String sheetName,
                          Class<?> headClass,
                          Consumer<ExcelWriterBuilder> writerBuilderCustomizer,
                          List<CellWriteHandler> handlers,
                          BatchDataLoader<T> loader,
                          int pageSize,
                          long maxRowsPerSheet,
                          long maxQueryPages) {
        check(outputStream != null, "outputStream不能为空");
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

        ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream, headClass);
        ExcelWriter writer = buildCustomizerExcelWriter(writerBuilder, writerBuilderCustomizer, handlers);
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

    /**
     * 文件输出流延迟打开：仅当 write(OutputStream...) 通过参数校验后才创建，
     * 避免参数非法时遗留空文件/流。
     */
    private static OutputStream fileOutputStream(File file) {
        try {
            return new java.io.FileOutputStream(file);
        } catch (java.io.IOException ex) {
            throw new ExImportingBusinessException("创建文件输出流失败：" + file, ex);
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

        writerBuilder.registerConverter(new BigDecimalFormatExcelConverter());
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
