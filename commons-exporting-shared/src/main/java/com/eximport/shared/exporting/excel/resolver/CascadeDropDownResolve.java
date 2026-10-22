package com.eximport.shared.exporting.excel.resolver;

import com.alibaba.excel.annotation.ExcelProperty;
import com.eximport.shared.exporting.excel.annotation.CascadeDropDown;
import com.eximport.shared.exporting.model.CascadeAnnotateMeta;

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
        Field[] fields = getAllFields(dtoCls);
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

    /**
     * è·åæ¬ç±»åå¨é¨ç¶ç±»çå¨é¨å±æ§ï¼ç¶ç±»å¨åï¼ä¸ hutool ReflectUtil.getFields è¯­ä¹ä¸è´ï¼ã
     */
    private static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                fieldList.add(field);
            }
        }
        return fieldList.toArray(new Field[0]);
    }
}
