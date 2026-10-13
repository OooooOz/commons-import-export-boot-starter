package com.commons.importing.configure;


import com.commons.importing.Importer;
import com.commons.importing.ImporterFactory;

public class DefaultImporterFactory<T> implements ImporterFactory<T> {

    private Class<T> entityClass;
    private Integer maxRows;

    public Importer<T> createImporter() {
        DefaultImporter<T> importer = new DefaultImporter<>(this);
        if (maxRows != null) {
            importer.maxRows(maxRows);
        }
        return importer;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }
}
