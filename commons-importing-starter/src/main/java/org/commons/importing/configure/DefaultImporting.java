package org.commons.importing.configure;

import java.util.concurrent.ConcurrentHashMap;

import org.commons.importing.Importer;
import org.commons.importing.ImporterFactory;
import org.commons.importing.ImporterFactoryBuilder;
import org.commons.importing.Importing;
import org.springframework.util.Assert;

/**
 * {@link Importing} 的默认实现。
 * <p>
 * 负责为不同实体类型缓存导入器工厂，并按需创建导入器实例。
 */
public class DefaultImporting implements Importing {

    private final ConcurrentHashMap<Class<?>, ImporterFactory<?>> importerFactories = new ConcurrentHashMap<>();

    /**
     * 创建默认的导入器工厂构建器。
     */
    public <T> ImporterFactoryBuilder<T> getImporterFactoryBuilder() {
        return new DefaultImporterFactoryBuilder<>();
    }

    /**
     * 获取指定实体类型的导入器。
     */
    public <T> Importer<T> getImporter(Class<T> entityClass) {
        Assert.notNull(entityClass, "entity class is null");
        ImporterFactory<T> importerFactory = getOrCreateImporterFactory(entityClass);
        return importerFactory.createImporter();
    }

    private <T> ImporterFactory<T> getOrCreateImporterFactory(Class<T> entityClass) {
        ImporterFactory<?> importerFactory = importerFactories.computeIfAbsent(entityClass,
                key -> createImporterFactory(entityClass));
        return castImporterFactory(importerFactory);
    }

    private <T> ImporterFactory<T> createImporterFactory(Class<T> entityClass) {
        ImporterFactoryBuilder<T> builder = new DefaultImporterFactoryBuilder<>();
        return builder.entityClass(entityClass).build();
    }

    @SuppressWarnings("unchecked")
    private <T> ImporterFactory<T> castImporterFactory(ImporterFactory<?> importerFactory) {
        return (ImporterFactory<T>) importerFactory;
    }
}
