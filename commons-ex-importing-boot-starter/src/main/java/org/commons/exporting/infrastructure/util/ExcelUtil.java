package org.commons.exporting.infrastructure.util;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @Description
 * @Author zengxj01
 * @Date 2022/2/16
 */
public final class ExcelUtil {
    private ExcelUtil() {}

    /**
     * 导出Excel，可自定义文件名、sheet表名、表头
     *
     * @param fileName 文件名
     * @param sheetName sheet表名
     * @param headClass 表头映射的实体类
     * @param dataList 导出数据的实体类集合
     * @param response
     */
    public static void exportExcel(String fileName, String sheetName, Class<?> headClass, List<?> dataList, HttpServletResponse response) {
        try {
            // 初始化响应头
            initResponseHeader(fileName, response);

            EasyExcelFactory.write(response.getOutputStream(), headClass).sheet(sheetName).doWrite(dataList);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 初始化响应头
     *
     * @param fileName 文件名
     * @param response
     */
    private static void initResponseHeader(String fileName, HttpServletResponse response) throws UnsupportedEncodingException {
        fileName = URLEncoder.encode(fileName, "UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("content-Type", "application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        response.setHeader("filename", fileName);
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    /**
     * 导出Excel，可自定义文件名、sheet表名、表头
     *
     * @param fileName  文件名
     * @param sheetName sheet表名
     * @param headClass 表头映射的实体类
     * @param dataList  导出数据的实体类集合
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
            throw new RuntimeException(e);
        }
    }
}
