package org.commons.importing.configure;


import org.commons.importing.Importer;
import org.commons.importing.ImporterFactory;

public class DefaultImporterFactory<T> implements ImporterFactory<T> {

    private Class<T> entityClass;

    public Importer<T> createImporter() {
        return new DefaultImporter(this);
    }

    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }
}
