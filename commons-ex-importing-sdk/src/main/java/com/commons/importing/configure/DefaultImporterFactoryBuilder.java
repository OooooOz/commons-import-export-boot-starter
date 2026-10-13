package com.commons.importing.configure;

import com.commons.importing.ImporterFactory;
import com.commons.importing.ImporterFactoryBuilder;

class DefaultImporterFactoryBuilder<T> implements ImporterFactoryBuilder<T> {
    private int maxRows;
    private Class<T> entityClass;

    DefaultImporterFactoryBuilder() {
    }

    public ImporterFactoryBuilder<T> maxRows(int maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public ImporterFactoryBuilder<T> entityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
        return this;
    }


    public ImporterFactory<T> build() {
        DefaultImporterFactory<T> factory = new DefaultImporterFactory<>();
        factory.setEntityClass(this.entityClass);
        if (this.maxRows > 0) {
            factory.setMaxRows(this.maxRows);
        }
        return factory;
    }
}
