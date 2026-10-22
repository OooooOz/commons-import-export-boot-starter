package com.eximport.export.shared.excel.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.commons.exporting.infrastructure.annotation.BigDecimalFormat;

import java.math.BigDecimal;

/**
 * Excel导出BigDecimal格式转换器：识别字段上的@BigDecimalFormat，按注解的scale与roundingMode先舍入再写出，
 * 使Jackson接口序列化与Excel导出的金额口径保持一致；无注解字段退回EasyExcel默认行为（原值写出）。
 * <p>
 * 注册于{@code ExcelUtil#streamWriteExcel}链路，不影响读导入及其他直接调用EasyExcelFactory的导出。
 *
 * @Description
 * @Author c-zhongwh01
 * @Date 2025/10/12
 */
public class BigDecimalFormatExcelConverter implements Converter<BigDecimal> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return BigDecimal.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public WriteCellData<?> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>(CellDataTypeEnum.EMPTY);
        }
        BigDecimalFormat format = resolveFormat(contentProperty);
        if (format == null) {
            // 无注解：保持EasyExcel默认行为，避免影响未标注字段的导出
            return new WriteCellData<>(value);
        }
        return new WriteCellData<>(value.setScale(format.scale(), format.roundingMode()));
    }

    /**
     * 从写出字段解析@BigDecimalFormat注解
     */
    private BigDecimalFormat resolveFormat(ExcelContentProperty contentProperty) {
        if (contentProperty == null || contentProperty.getField() == null) {
            return null;
        }
        return contentProperty.getField().getAnnotation(BigDecimalFormat.class);
    }
}
