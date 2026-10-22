package com.eximport.shared.exporting.excel.annotation;

import org.apache.poi.ss.usermodel.Font;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelComment {

    /** 批注内容 */
    String value() default "";

    /**
     * 批注横跨多少列
     *
     * @return
     */
    int columnIndex() default 2;

    /**
     * 批注纵多少行
     *
     * @return
     */
    int rowIndex() default 3;

    /**
     * 批注颜色
     *
     * @return
     */
    short fontColor() default Font.COLOR_NORMAL;
}
