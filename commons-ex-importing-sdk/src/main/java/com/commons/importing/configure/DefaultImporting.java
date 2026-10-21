package com.commons.importing.configure;

import java.util.concurrent.ConcurrentHashMap;

import com.commons.importing.Importer;
import com.commons.importing.ImporterFactory;
import com.commons.importing.ImporterFactoryBuilder;
import com.commons.importing.Importing;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;

public class DefaultImporting implements Importing, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final ConcurrentHashMap<Class<?>, ImporterFactory<?>> importerFactories = new ConcurrentHashMap();

    public <T> ImporterFactoryBuilder<T> getImporterFactoryBuilder() {
        return new DefaultImporterFactoryBuilder(this);
    }

    public <T> Importer<T> getImporter(Class<T> entityClass) {
        Assert.notNull(entityClass, "entity class is null");
        ImporterFactory<T> importerFactory = (ImporterFactory)this.importerFactories.get(entityClass);
        if (importerFactory == null) {
            ImporterFactoryBuilder<T> builder = getImporterFactoryBuilder();
            importerFactory = builder.entityClass(entityClass).build();
            this.importerFactories.put(entityClass, importerFactory);
        }
        return importerFactory.createImporter();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
