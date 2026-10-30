package com.commons.exporting.infrastructure.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.eximport.shared.exporting.excel.ExcelWriterUtils;
import com.eximport.shared.exporting.excel.loader.BatchDataLoader;
import com.eximport.shared.exporting.exception.ExImportingBusinessException;
import com.eximport.shared.exporting.model.BatchData;
import com.eximport.shared.exporting.model.MultiSheetWriterSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

/**
 * @Description
 * @Author zengxj01
 * @Date 2022/2/16
 */
@Slf4j
public final class CommonExcelUtil {
    private CommonExcelUtil() {
    }

    /**
     * Excel Content-Type
     */
    public static final String APPLICATION_EXCEL = "application/vnd.ms-excel;charset=utf-8";

    /**
     * Content-Disposition 响应头
     */
    public static final String CONTENT_DISPOSITION = "Content-disposition";

    /**
     * 导出Excel，可自定义文件名、sheet表名、表头
     *
     * @param fileName  文件名
     * @param sheetName sheet表名
     * @param headClass 表头映射的实体类
     * @param dataList  导出数据的实体类集合
     * @param response
     */
    public static void exportExcel(String fileName, String sheetName, Class<?> headClass, List<?> dataList, HttpServletResponse response) {
        try {
            // 初始化响应头
            initResponseHeader(fileName, response);

            EasyExcelFactory.write(response.getOutputStream(), headClass).sheet(sheetName).doWrite(dataList);
        } catch (Exception e) {
            throw new ExImportingBusinessException(e.getMessage());
        }
    }

    /**
     * 初始化响应头
     *
     * @param fileName 文件名
     * @param response
     */
    public static void initResponseHeader(String fileName, HttpServletResponse response) throws UnsupportedEncodingException {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setHeader("filename", fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    public static InputStream getInputStreamByFile(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (IOException e) {
            log.info("获取文件信息有误：{}", e.getMessage(), e);
            throw new ExImportingBusinessException("获取文件信息有误");
        }
    }

    /**
     * Excel单元格超长文本截断（兼容Excel 32767字符限制）
     *
     * @param text 原始文本
     * @return 处理后文本
     */
    public static String cutExcelCellText(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        final int MAX_SAFE_LEN = 32764;
        if (text.length() <= MAX_SAFE_LEN) {
            return text;
        }
        return StrUtil.sub(text, 0, MAX_SAFE_LEN) + "...";
    }


    /**
     * 导出Excel并封装为MultipartFile，可自定义文件名、sheet表名、表头
     *
     * @param excelWriterBuilder ExcelWriterBuilder
     * @param fileName           文件名
     * @param sheetName          sheet表名
     * @param headClass          表头映射的实体类
     * @param dataList           导出数据的实体类集合
     */
    public static MultipartFile excelWriterBuilder2MultipartFile(ExcelWriterBuilder excelWriterBuilder, String fileName, String sheetName, Class headClass, List dataList) {
        try {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            excelWriterBuilder.file(bos);
            if (headClass != null) {
                excelWriterBuilder.head(headClass);
            }
            excelWriterBuilder.sheet(sheetName).doWrite(dataList);
            byte[] bytes = bos.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(bytes);
            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, MediaType.MULTIPART_FORM_DATA_VALUE, inputStream);
            return multipartFile;
        } catch (Exception e) {
            throw new ExImportingBusinessException(e.getMessage(), e);
        }
    }

    /**
     * 分页流式导出Excel：逐批查询逐批写入，避免一次性加载过多数据
     *
     * @param fileName  文件名
     * @param sheetName sheet表名
     * @param headClass 表头映射的实体类
     * @param pageSize  单批条数
     * @param loader    分批数据加载器（返回空或不足单批条数即结束；已知总页数时整页结尾提前终止）
     * @param response  响应
     * @param <T>       导出行类型
     */
    public static <T> void streamWriteExcel(String fileName, String sheetName, Class<?> headClass, int pageSize,
                                            BatchDataLoader<T> loader, HttpServletResponse response) {
        streamWriteExcel(fileName, sheetName, headClass, pageSize, loader, null, response);
    }

    /**
     * 分页流式导出Excel：逐批查询逐批写入，避免一次性加载过多数据
     *
     * @param fileName  文件名
     * @param sheetName sheet表名
     * @param headClass 表头映射的实体类
     * @param pageSize  单批条数
     * @param loader    分批数据加载器（返回空或不足单批条数即结束；已知总页数时整页结尾提前终止）
     * @param handlers  单元格写入处理器列表（可为空；可多个，按列表顺序依次注册生效）
     * @param response  响应
     * @param <T>       导出行类型
     */
    public static <T> void streamWriteExcel(String fileName,
                                            String sheetName,
                                            Class<?> headClass,
                                            int pageSize,
                                            BatchDataLoader<T> loader,
                                            List<CellWriteHandler> handlers,
                                            HttpServletResponse response) {
        if (pageSize <= 0) {
            throw new ExImportingBusinessException("导出单批条数必须大于0");
        }
        try {
            initResponseHeader(fileName, response);
            // 复用 shared 模块 LargeExcelWriter 的分页调度/多sheet/终止规则逻辑
            new ExcelWriterUtils().write(
                    response.getOutputStream(),
                    sheetName,
                    headClass,
                    null,
                    handlers,
                    loader,
                    pageSize,
                    ExcelWriterUtils.MAX_ROWS_PER_SHEET,
                    Integer.MAX_VALUE);
        } catch (ExImportingBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel流式导出失败：{}", fileName, e);
            throw new ExImportingBusinessException(e.getMessage());
        }
    }

    /**
     * 分页流式导出多sheet Excel：按配置顺序逐sheet逐批查询逐批写入，避免一次性加载过多数据。
     *
     * <p>每个sheet独立配置表头类与分批数据加载器，终止规则与{@link #streamWriteExcel}一致：
     * 该sheet的loader行数据为空、或行数不足该sheet单批条数时结束当前sheet；已知总页数时整页结尾提前终止；
     * 数据为空时写出仅表头的空sheet。</p>
     *
     * <p>每个sheet可配置多个写入处理器（SheetWriteHandler/CellWriteHandler等）及表头下移行数
     * （用于表头上方写入说明行等场景）。</p>
     *
     * <p>单个sheet数据行数超过 maxRowsPerSheet 时自动切分新sheet继续写入，切分行为参考
     * 命名追加 "_序号" 后缀（见{@link ExcelWriterUtils#buildSheetName}），切分出的每个sheet
     * 均重复注册该sheet的处理器与表头下移行数，保证各分片可独立阅读。</p>
     *
     * @param fileName 文件名
     * @param response 响应
     * @param sheets   各sheet配置（sheet名、表头类、单批条数、分批数据加载器、写入处理器列表、表头下移行数、
     *                 单sheet最大行数，处理器可为空，最大行数&lt;=0时取默认值）
     */
    public static void streamWriteExcelMultiSheet(String fileName,
                                                  HttpServletResponse response,
                                                  List<MultiSheetWriterSpec> sheets) {
        if (sheets == null || sheets.isEmpty()) {
            throw new ExImportingBusinessException("导出sheet配置不能为空");
        }
        ExcelWriter excelWriter = null;
        try {
            initResponseHeader(fileName, response);
            excelWriter = EasyExcelFactory.write(response.getOutputStream()).inMemory(false).build();
            // sheet下标随切分递增，跨spec连续
            int sheetIndex = 0;
            for (MultiSheetWriterSpec spec : sheets) {
                if (spec.getPageSize() <= 0) {
                    throw new ExImportingBusinessException("导出单批条数必须大于0");
                }
                long maxRowsPerSheet = resolveMaxRowsPerSheet(spec);
                // sheet切分序号：0表示首个sheet保持原名
                int sheetNo = 0;
                WriteSheet currentSheet = buildSpecWriteSheet(sheetIndex, sheetNo, spec);
                long currentSheetRows = 0L;
                int pageNum = 1;
                while (true) {
                    BatchData<?> batchData = spec.getLoader().load(pageNum);
                    List<?> batch = batchData == null ? null : batchData.getRows();
                    if (batch == null || batch.isEmpty()) {
                        if (pageNum == 1) {
                            excelWriter.write(Collections.emptyList(), currentSheet);
                        }
                        break;
                    }
                    int size = batch.size();
                    if (currentSheetRows + size <= maxRowsPerSheet) {
                        // 常规路径：整批装入当前 sheet，无需切分
                        excelWriter.write(batch, currentSheet);
                        currentSheetRows += size;
                    } else {
                        // 跨sheet路径：按当前sheet剩余容量切分写入，写满自动新建sheet
                        int offset = 0;
                        while (offset < size) {
                            if (currentSheetRows >= maxRowsPerSheet) {
                                sheetIndex++;
                                sheetNo++;
                                currentSheetRows = 0L;
                                currentSheet = buildSpecWriteSheet(sheetIndex, sheetNo, spec);
                            }
                            int canWrite = (int) Math.min((long) size - offset, maxRowsPerSheet - currentSheetRows);
                            excelWriter.write(batch.subList(offset, offset + canWrite), currentSheet);
                            currentSheetRows += canWrite;
                            offset += canWrite;
                        }
                    }
                    // 不足整页说明已是最后一页，停止继续查询
                    if (batch.size() < spec.getPageSize()) {
                        break;
                    }
                    // 已知总页数且当前已是最后一页，无需再多一次空探测
                    if (batchData.getTotalPages() > 0 && pageNum >= batchData.getTotalPages()) {
                        break;
                    }
                    pageNum++;
                }
                sheetIndex++;
            }
        } catch (ExImportingBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel多sheet流式导出失败：{}", fileName, e);
            throw new ExImportingBusinessException(e.getMessage());
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 解析单sheet最大数据行数：显式配置时不超过 xlsx 上限扣除表头下移行数（首个表头行已在上限中预留），
     * 未配置（&lt;=0）时直接取该上限
     *
     * @param spec sheet配置
     * @return 单sheet最大数据行数
     */
    private static long resolveMaxRowsPerSheet(MultiSheetWriterSpec spec) {
        long maxSheetRows = ExcelWriterUtils.MAX_ROWS_PER_SHEET - Math.max(0, spec.getRelativeHeadRowIndex());
        return spec.getMaxRowsPerSheet() > 0 ? Math.min(spec.getMaxRowsPerSheet(), maxSheetRows) : maxSheetRows;
    }

    /**
     * 按spec配置构建单个写入sheet，切分出的后续sheet以 "_序号" 后缀命名
     *
     * @param sheetIndex 工作簿内sheet下标（随切分递增）
     * @param sheetNo    该spec内的切分序号，0表示首个sheet
     * @param spec       sheet配置
     * @return 写入sheet
     */
    private static WriteSheet buildSpecWriteSheet(int sheetIndex, int sheetNo, MultiSheetWriterSpec spec) {
        ExcelWriterSheetBuilder sheetBuilder = EasyExcelFactory.writerSheet(sheetIndex, ExcelWriterUtils.buildSheetName(sheetNo, spec.getSheetName()))
                .head(spec.getHeadClass());
        if (spec.getRelativeHeadRowIndex() > 0) {
            sheetBuilder.relativeHeadRowIndex(spec.getRelativeHeadRowIndex());
        }
        if (spec.getHandlers() != null && !spec.getHandlers().isEmpty()) {
            for (WriteHandler handler : spec.getHandlers()) {
                sheetBuilder.registerWriteHandler(handler);
            }
        }
        return sheetBuilder.build();
    }
}
