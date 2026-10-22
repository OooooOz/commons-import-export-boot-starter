package com.eximport.shared.exporting.model;


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

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    public int getDependCol() {
        return dependCol;
    }

    public void setDependCol(int dependCol) {
        this.dependCol = dependCol;
    }

    public String getDictKey() {
        return dictKey;
    }

    public void setDictKey(String dictKey) {
        this.dictKey = dictKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
