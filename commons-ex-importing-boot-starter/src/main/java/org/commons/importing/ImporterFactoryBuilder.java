package org.commons.importing;

public interface ImporterFactoryBuilder<T> {
    ImporterFactoryBuilder<T> maxRows(int paramInt);

    ImporterFactoryBuilder<T> entityClass(Class<T> paramClass);

    ImporterFactory<T> build();
}
