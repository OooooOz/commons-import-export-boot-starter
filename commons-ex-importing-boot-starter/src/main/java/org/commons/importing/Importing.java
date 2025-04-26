package org.commons.importing;

public abstract interface Importing {

    public abstract <T> ImporterFactoryBuilder<T> getImporterFactoryBuilder();

    public abstract <T> Importer<T> getImporter(Class<T> paramClass);
}
