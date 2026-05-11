package org.commons.importing;

/**
 * 导入 starter 门面接口。
 * <p>
 * 业务系统可直接注入该接口获取导入器工厂构建器，或按实体类型获取默认导入器实例。
 */
public interface Importing {

    /**
     * 创建导入器工厂构建器。
     *
     * @param <T> 导入实体类型
     * @return 导入器工厂构建器
     */
    <T> ImporterFactoryBuilder<T> getImporterFactoryBuilder();

    /**
     * 按实体类型获取默认导入器。
     *
     * @param entityClass 导入实体类型
     * @param <T> 导入实体类型
     * @return 对应实体的导入器实例
     */
    <T> Importer<T> getImporter(Class<T> entityClass);
}
