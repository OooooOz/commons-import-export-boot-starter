package com.eximport.shared.exporting.excel.resolver;

import com.alibaba.excel.annotation.ExcelProperty;
import com.eximport.shared.exporting.excel.annotation.ExcelDynamicSelect;
import com.eximport.shared.exporting.excel.annotation.ExcelSelected;
import com.eximport.shared.exporting.model.ExcelSelectedMeta;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author zengxj01
 * @Date 2022/2/16
 */
@Slf4j
public final class ExcelSelectedResolver {
    private ExcelSelectedResolver() {
    }

    /**
     * 解析表头类中的下拉注解
     *
     * @param head 表头类
     * @param <T>  泛型
     * @return Map<下拉框列索引, 下拉框内容> map
     */
    public static <T> Map<Integer, ExcelSelectedMeta> resolveSelectedAnnotation(Class<T> head) {
        Map<Integer, ExcelSelectedMeta> selectedMap = new HashMap<>();

        // getDeclaredFields(): 返回全部声明的属性；getFields(): 返回public类型的属性
        Field[] fields = head.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            // 解析注解信息
            ExcelSelected selected = field.getAnnotation(ExcelSelected.class);
            ExcelProperty property = field.getAnnotation(ExcelProperty.class);
            if (selected != null) {
                ExcelSelectedMeta selectedMeta = new ExcelSelectedMeta();
                String[] source = resolveSelectedSource(selected);
                if (source != null && source.length > 0) {
                    selectedMeta.setSource(source);
                    selectedMeta.setFirstRow(selected.firstRow());
                    selectedMeta.setLastRow(selected.lastRow());
                    if (property != null && property.index() >= 0) {
                        selectedMap.put(property.index(), selectedMeta);
                    } else {
                        selectedMap.put(i, selectedMeta);
                    }
                }
            }
        }
        return selectedMap;
    }


    public static String[] resolveSelectedSource(ExcelSelected excelSelected) {
        if (excelSelected == null) {
            return new String[0];
        }

        // 获取固定下拉框的内容
        String[] annotationSource = excelSelected.source();
        if (annotationSource.length > 0) {
            return annotationSource;
        }

        // 获取动态下拉框的内容
        Class<? extends ExcelDynamicSelect>[] classes = excelSelected.sourceClass();
        if (classes.length > 0) {
            try {
                ExcelDynamicSelect excelDynamicSelect = classes[0].getDeclaredConstructor().newInstance();
                String[] dynamicSelectSource = excelDynamicSelect.getSource();
                if (dynamicSelectSource != null && dynamicSelectSource.length > 0) {
                    return dynamicSelectSource;
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("解析动态下拉框数据异常", e);
            }
        }
        return new String[0];
    }

}
