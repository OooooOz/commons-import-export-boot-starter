package com.commons.importing;

import com.commons.importing.configure.AbstractCommonDataListener;
import com.commons.importing.model.ImportResultVO;

import java.io.InputStream;

public interface Importer<T> {

    void file(InputStream file);

    void maxRows(Integer maxRows);

    void readAll(boolean readAll);

    void startImport(AbstractCommonDataListener listener);

    ImportResultVO getImportResultVO();
}
