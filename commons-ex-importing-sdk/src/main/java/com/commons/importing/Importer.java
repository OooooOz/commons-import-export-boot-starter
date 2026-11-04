package com.commons.importing;

import com.commons.importing.configure.AbstractCommonDataListener;
import com.commons.importing.model.ImportResultVO;

import java.io.InputStream;

public interface Importer<T> {

    void file(InputStream file);

    void maxRows(Integer maxRows);

    void readAll(boolean readAll);

    /**
     * 设置表头占用的行数，用于首个表头行之前存在说明行等非表头内容的场景
     * <p>EasyExcel 默认表头占 1 行；模板首行为说明行时表头实际从第 2 行开始，须设置为 2。
     * 不设置或设置的值小于等于 0 时按 EasyExcel 默认值处理。</p>
     *
     * @param headRowNumber 表头行数
     */
    void headRowNumber(Integer headRowNumber);

    void startImport(AbstractCommonDataListener listener);

    ImportResultVO getImportResultVO();
}
