package com.commons.exporting.infrastructure.handle;

import cn.hutool.core.util.ReflectUtil;
import com.alibaba.excel.annotation.ExcelProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author c-zhongwh01
 * @Date 2026/8/26 13:47
 */
public class CascadeDropDownResolve {

    /**
     * 解析DTO class，拿到所有@CascadeDropDown注解元信息
     */
    public static List<CascadeAnnotateMeta> resolveMetaData(Class<?> dtoCls) {
        List<CascadeAnnotateMeta> metaList = new ArrayList<>();
        Field[] fields = ReflectUtil.getFields(dtoCls);
        for (Field field : fields) {
            CascadeDropDown anno = field.getAnnotation(CascadeDropDown.class);
            if (anno == null) {
                continue;
            }
            ExcelProperty excelProp = field.getAnnotation(ExcelProperty.class);
            if (excelProp == null) {
                continue;
            }
            CascadeAnnotateMeta meta = new CascadeAnnotateMeta();
            meta.setColumnIndex(excelProp.index());
            meta.setParent(anno.isParent());
            meta.setDependCol(anno.dependOnCol());
            meta.setDictKey(anno.dictKey());
            meta.setFieldName(field.getName());
            metaList.add(meta);
        }
        return metaList;
    }
}
