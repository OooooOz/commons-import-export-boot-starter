package org.commons.importing;

import java.io.InputStream;
import java.util.function.Consumer;

import org.commons.importing.configure.AbstractCommonDataListener;
import org.commons.importing.model.ImportResultVO;

public interface Importer<T> {

    void file(InputStream file);

    void maxRows(Integer maxRows);

    void startImport(AbstractCommonDataListener listener);

    ImportResultVO getImportResultVO();
}
