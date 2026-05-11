package org.commons.importing.configure;


import java.io.InputStream;

import org.commons.importing.Importer;
import org.commons.importing.model.ImportResultVO;

import com.alibaba.excel.EasyExcelFactory;

public class DefaultImporter<T> implements Importer<T> {

    private DefaultImporterFactory<T> factory;

    private InputStream file;

    private Integer maxRows;

    private ImportResultVO importResultVO;

    public DefaultImporter(DefaultImporterFactory factory) {
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
    public void startImport(AbstractCommonDataListener listener) {
        listener.setMaxRows(maxRows);
        EasyExcelFactory.read(file, factory.getEntityClass(), listener).sheet().doRead();
        importResultVO = listener.getImportResultVO();
    }

    @Override
    public ImportResultVO getImportResultVO() {
        return importResultVO;
    }
}
