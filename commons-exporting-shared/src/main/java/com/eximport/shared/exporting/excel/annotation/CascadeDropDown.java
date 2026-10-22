package com.eximport.shared.exporting.excel.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CascadeDropDown {

    /**
     * 是否父级（一级下拉）
     */
    boolean isParent() default false;

    /**
     * 依赖的列索引，子级才生效，对应父字段所在列
     */
    int dependOnCol() default -1;

    /**
     * 字典标识key，用于区分多组不同级联字典
     */
    String dictKey();
}
