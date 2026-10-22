package com.commons.importing.configure;

import com.alibaba.excel.EasyExcelFactory;
import com.commons.importing.Importer;
import com.commons.importing.model.ImportResultVO;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

public class DefaultImporter<T> implements Importer<T> {

    private final DefaultImporterFactory<T> factory;

    private InputStream file;

    private Integer maxRows;

    private boolean readAll;

    private String sheetName;

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
    public void readAll(boolean readAll) {
        this.readAll = readAll;
    }

    @Override
    public void startImport(AbstractCommonDataListener listener) {
        listener.setMaxRows(maxRows);
        if (!readAll) {
            if (StringUtils.isNotBlank(sheetName)) {
                EasyExcelFactory.read(file, factory.getEntityClass(), listener).sheet(sheetName).doRead();
            } else {
                EasyExcelFactory.read(file, factory.getEntityClass(), listener).sheet().doRead();
            }
        } else {
            EasyExcelFactory.read(file, factory.getEntityClass(), listener).doReadAll();
        }
        importResultVO = listener.getImportResultVO();
    }

    @Override
    public ImportResultVO getImportResultVO() {
        return importResultVO;
    }
}
