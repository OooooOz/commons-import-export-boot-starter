package com.eximport.shared.exporting.excel.converter;

import com.alibaba.excel.converters.booleanconverter.BooleanStringConverter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.StringUtils;

import java.util.Locale;

/**
 * 自定义 Boolean-String 读取转换器：在 EasyExcel 默认行为之上扩展中文识别。
 * <p>
 * 单元格值为"是"（忽略首尾空白与大小写）时读为 TRUE，"否"时读为 FALSE；
 * 空白单元格读为 null（交由字段自身语义决定是否必填）；
 * 其余值（如 TRUE/FALSE/1/0）退回 {@link BooleanStringConverter} 默认解析。
 * <p>
 * 适用于导入场景读取"是/否"列；导出写出仍走父类默认行为。
 *
 * @Description
 * @Author c-zhongwh01
 * @Date 2025/8/24 19:55
 */
public class CustomerBooleanStringConverter extends BooleanStringConverter {

    private static final String TRUE_TEXT = "是";
    private static final String FALSE_TEXT = "否";

    @Override
    public Boolean convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        String value = cellData.getStringValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String normalized = value.trim();
        if (TRUE_TEXT.equals(normalized)) {
            return Boolean.TRUE;
        }
        if (FALSE_TEXT.equals(normalized)) {
            return Boolean.FALSE;
        }
        return super.convertToJavaData(cellData, contentProperty, globalConfiguration);
    }

    /**
     * 提供给写入侧可选复用的中文文案：TRUE→"是"，FALSE→"否"，null→null。
     */
    public String convertToExcelText(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? TRUE_TEXT : FALSE_TEXT;
    }

    /**
     * 大小写不敏感的宽松匹配（可选增强，供特殊导入场景使用）。
     */
    static boolean matchesIgnoreCase(String value, String expected) {
        return value != null && value.trim().toLowerCase(Locale.ROOT).equals(expected);
    }
}
