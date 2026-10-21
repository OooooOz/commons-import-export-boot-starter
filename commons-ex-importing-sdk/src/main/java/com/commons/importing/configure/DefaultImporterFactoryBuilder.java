package com.commons.importing.configure;

import com.commons.importing.ImporterFactory;
import com.commons.importing.ImporterFactoryBuilder;

class DefaultImporterFactoryBuilder<T> implements ImporterFactoryBuilder<T> {
    private int maxRows;
    private DefaultImporting importing;
    private Class<T> entityClass;

    DefaultImporterFactoryBuilder(DefaultImporting importing) {
        this.importing = importing;
    }

    public ImporterFactoryBuilder<T> maxRows(int paramInt) {
        this.maxRows = paramInt;
        return this;
    }

    public ImporterFactoryBuilder<T> entityClass(Class<T> paramClass) {
        this.entityClass = paramClass;
        return this;
    }

    public ImporterFactory<T> build() {
        DefaultImporterFactory factory = new DefaultImporterFactory();
        factory.setEntityClass(this.entityClass);
        return (ImporterFactory<T>)factory;
    }
}
