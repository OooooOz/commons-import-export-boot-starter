package org.commons.importing.configure;


import java.io.InputStream;

import org.commons.importing.Importer;
import org.commons.importing.model.ImportResultVO;
import org.springframework.util.Assert;

import com.alibaba.excel.EasyExcelFactory;

/**
 * 默认导入器实现。
 * <p>
 * 基于 EasyExcel 读取文件流，并委托 {@link AbstractCommonDataListener} 完成校验、入库与结果统计。
 *
 * @param <T> 导入实体类型
 */
public class DefaultImporter<T> implements Importer<T> {

    private final DefaultImporterFactory<T> factory;

    private InputStream file;

    private Integer maxRows;

    private ImportResultVO importResultVO;

    public DefaultImporter(DefaultImporterFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public void file(InputStream file) {
        this.file = file;
    }

    @Override
    public void maxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }

    @Override
    public void startImport(AbstractCommonDataListener<T> listener) {
        Assert.notNull(listener, "listener must not be null");
        Assert.notNull(file, "import file must not be null");
        Assert.notNull(factory.getEntityClass(), "entity class must not be null");

        importResultVO = new ImportResultVO();
        listener.setMaxRows(maxRows);
        EasyExcelFactory.read(file, factory.getEntityClass(), listener).sheet().doRead();
        importResultVO = listener.getImportResultVO();
    }

    @Override
    public ImportResultVO getImportResultVO() {
        return importResultVO;
    }
}
