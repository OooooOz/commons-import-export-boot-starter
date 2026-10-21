package com.commons.exporting.infrastructure.converter;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.excel.converters.booleanconverter.BooleanStringConverter;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 * @Description
 * @Author c-zhongwh01
 * @Date 2025/8/24 19:55
 */
public class CustomerBooleanStringConverter extends BooleanStringConverter {

    @Override
    public Boolean convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        String value = cellData.getStringValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (List.of("是").contains(value)) {
            return Boolean.TRUE;
        } else if (List.of("否").contains(value)) {
            return Boolean.FALSE;
        } else {
            return super.convertToJavaData(cellData, contentProperty, globalConfiguration);
        }
    }
}
