package com.commons.exporting.infrastructure.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.commons.exporting.infrastructure.converter.BigDecimalFormatExcelConverter;
import com.commons.exporting.infrastructure.exception.ExImportingBusinessException;
import com.commons.exporting.infrastructure.loader.BatchDataLoader;
import com.commons.exporting.infrastructure.model.BatchData;
import com.commons.exporting.infrastructure.model.MultiSheetWriterSpec;
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
        ExcelWriter excelWriter = null;
        try {
            initResponseHeader(fileName, response);
            ExcelWriterBuilder writerBuilder = EasyExcelFactory.write(response.getOutputStream(), headClass).inMemory(false);
            // 注册BigDecimal格式转换器：识别@BigDecimalFormat按注解scale/roundingMode舍入后写出
            writerBuilder.registerConverter(new BigDecimalFormatExcelConverter());
            if (handlers != null) {
                for (CellWriteHandler handler : handlers) {
                    if (handler != null) {
                        writerBuilder.registerWriteHandler(handler);
                    }
                }
            }
            excelWriter = writerBuilder.build();
            WriteSheet writeSheet = EasyExcelFactory.writerSheet(sheetName).build();
            int pageNum = 1;
            while (true) {
                BatchData<T> batchData = loader.load(pageNum);
                List<T> batch = batchData == null ? null : batchData.getRows();
                if (batch == null || batch.isEmpty()) {
                    if (pageNum == 1) {
                        excelWriter.write(Collections.emptyList(), writeSheet);
                    }
                    break;
                }
                excelWriter.write(batch, writeSheet);
                // 不足整页说明已是最后一页，停止继续查询
                if (batch.size() < pageSize) {
                    break;
                }
                // 已知总页数且当前已是最后一页，无需再多一次空探测
                if (batchData.getTotalPages() > 0 && pageNum >= batchData.getTotalPages()) {
                    break;
                }
                pageNum++;
            }
        } catch (ExImportingBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel流式导出失败：{}", fileName, e);
            throw new ExImportingBusinessException(e.getMessage());
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    /**
     * 分页流式导出多sheet Excel：按配置顺序逐sheet逐批查询逐批写入，避免一次性加载过多数据。
     *
     * <p>每个sheet独立配置表头类与分批数据加载器，终止规则与{@link #streamWriteExcel}一致：
     * 该sheet的loader行数据为空、或行数不足该sheet单批条数时结束当前sheet；已知总页数时整页结尾提前终止；
     * 数据为空时写出仅表头的空sheet。</p>
     *
     * @param fileName 文件名
     * @param response 响应
     * @param sheets   各sheet配置（sheet名、表头类、单批条数、分批数据加载器、单元格写入处理器，处理器可为空）
     */
    public static void streamWriteExcelMultiSheet(String fileName, HttpServletResponse response, List<MultiSheetWriterSpec> sheets) {
        if (sheets == null || sheets.isEmpty()) {
            throw new ExImportingBusinessException("导出sheet配置不能为空");
        }
        ExcelWriter excelWriter = null;
        try {
            initResponseHeader(fileName, response);
            excelWriter = EasyExcelFactory.write(response.getOutputStream()).inMemory(false).build();
            for (int sheetIndex = 0; sheetIndex < sheets.size(); sheetIndex++) {
                MultiSheetWriterSpec spec = sheets.get(sheetIndex);
                if (spec.getPageSize() <= 0) {
                    throw new ExImportingBusinessException("导出单批条数必须大于0");
                }
                ExcelWriterSheetBuilder sheetBuilder = EasyExcelFactory.writerSheet(sheetIndex, spec.getSheetName())
                        .head(spec.getHeadClass());
                if (spec.getHandler() != null) {
                    sheetBuilder.registerWriteHandler(spec.getHandler());
                }
                WriteSheet writeSheet = sheetBuilder.build();
                int pageNum = 1;
                while (true) {
                    BatchData<?> batchData = spec.getLoader().load(pageNum);
                    List<?> batch = batchData == null ? null : batchData.getRows();
                    if (batch == null || batch.isEmpty()) {
                        if (pageNum == 1) {
                            excelWriter.write(Collections.emptyList(), writeSheet);
                        }
                        break;
                    }
                    excelWriter.write(batch, writeSheet);
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
}
