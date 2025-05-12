package org.commons.exporting.infrastructure.handle;

public interface ExcelDynamicSelect {
    /**
     * 获取动态生成的下拉框可选数据
     *
     * @return 动态生成的下拉框可选数据
     */
    String[] getSource();
}
