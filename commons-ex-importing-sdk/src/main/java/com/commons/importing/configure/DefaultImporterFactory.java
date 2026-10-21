package com.commons.importing.configure;

import com.commons.importing.Importer;
import com.commons.importing.ImporterFactory;

public class DefaultImporterFactory<T> implements ImporterFactory<T> {

    private Class<T> entityClass;

    public Importer<T> createImporter() {
        return new DefaultImporter(this);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
}
