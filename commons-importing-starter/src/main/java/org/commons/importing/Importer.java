package org.commons.importing;

import java.io.InputStream;

import org.commons.importing.configure.AbstractCommonDataListener;
import org.commons.importing.model.ImportResultVO;

/**
 * 导入执行器。
 * <p>
 * 负责接收待导入文件流、最大导入行数以及导入监听器，并触发 EasyExcel 读取流程。
 *
 * @param <T> 导入实体类型
 */
public interface Importer<T> {

    /**
     * 设置待导入的 Excel 文件输入流。
     *
     * @param file Excel 文件输入流
     */
    void file(InputStream file);

    /**
     * 设置最大允许导入的行数。
     *
     * @param maxRows 最大行数，允许为空表示不限制
     */
    void maxRows(Integer maxRows);

    /**
     * 开始执行导入。
     *
     * @param listener 导入监听器，负责校验、落库与结果汇总
     */
    void startImport(AbstractCommonDataListener<T> listener);

    /**
     * 获取最近一次导入执行结果。
     *
     * @return 导入结果
     */
    ImportResultVO getImportResultVO();
}
