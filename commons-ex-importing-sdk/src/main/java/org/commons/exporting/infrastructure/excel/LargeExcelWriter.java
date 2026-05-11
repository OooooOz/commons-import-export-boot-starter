package org.commons.exporting.infrastructure.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * 大文件 Excel 分页写入工具，避免一次性把全部数据放入内存。
 */
public class LargeExcelWriter {

    public interface PageDataLoader<T> {
        List<T> load(long pageNo, int pageSize);
    }

    public <T> void write(File file,
                          String sheetName,
                          Class<T> headClass,
                          Consumer<ExcelWriterBuilder> writerBuilderCustomizer,
                          PageDataLoader<T> loader,
                          int pageSize,
                          long maxRowsPerSheet) {
        if (file == null) {
            throw new IllegalArgumentException("file不能为空");
        }
        if (headClass == null) {
            throw new IllegalArgumentException("headClass不能为空");
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader不能为空");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize必须大于0");
        }
        if (maxRowsPerSheet <= 0 || maxRowsPerSheet > 1048575L) {
            maxRowsPerSheet = 1048575L;
        }

        ExcelWriterBuilder writerBuilder = EasyExcel.write(file, headClass);
        if (writerBuilderCustomizer != null) {
            writerBuilderCustomizer.accept(writerBuilder);
        }
        ExcelWriter writer = writerBuilder.build();
        try {
            int sheetNo = 0;
            long currentSheetRows = 0L;
            boolean wroteAny = false;
            WriteSheet currentSheet = buildSheet(sheetNo, sheetName);

            long pageNo = 1L;
            while (true) {
                List<T> pageData = loader.load(pageNo, pageSize);
                if (pageData == null || pageData.isEmpty()) {
                    break;
                }

                int offset = 0;
                while (offset < pageData.size()) {
                    if (currentSheetRows >= maxRowsPerSheet) {
                        sheetNo++;
                        currentSheetRows = 0L;
                        currentSheet = buildSheet(sheetNo, sheetName);
                    }
                    int canWrite = (int) Math.min((long) pageData.size() - offset, maxRowsPerSheet - currentSheetRows);
                    List<T> segment = pageData.subList(offset, offset + canWrite);
                    writer.write(segment, currentSheet);
                    wroteAny = true;
                    currentSheetRows += segment.size();
                    offset += canWrite;
                }

                if (pageData.size() < pageSize) {
                    break;
                }
                pageNo++;
            }

            if (!wroteAny) {
                writer.write(Collections.emptyList(), currentSheet);
            }
        } finally {
            writer.finish();
        }
    }

    private WriteSheet buildSheet(int sheetNo, String sheetName) {
        String name = sheetName == null || sheetName.trim().length() == 0 ? "数据" : sheetName.trim();
        if (sheetNo > 0) {
            name = name + "_" + (sheetNo + 1);
        }
        return EasyExcel.writerSheet(sheetNo, name).build();
    }
}

