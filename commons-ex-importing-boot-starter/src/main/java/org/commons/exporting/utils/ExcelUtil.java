package org.commons.exporting.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.excel.EasyExcelFactory;

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
}
