package com.commons.exporting.infrastructure.handle;

import lombok.Data;

@Data
public class CascadeAnnotateMeta {
    /**
     * excel列下标
     */
    private int columnIndex;
    /**
     * 是否父级
     */
    private boolean parent;
    /**
     * 依赖父列下标
     */
    private int dependCol;
    /**
     * 字典key
     */
    private String dictKey;
    /**
     * 字段名称，写入时用于解析真实列下标（未显式指定@ExcelProperty#index时columnIndex为-1）
     */
    private String fieldName;
}
