package org.commons.importing;

/**
 * 导入器工厂构建器。
 *
 * @param <T> 导入实体类型
 */
public interface ImporterFactoryBuilder<T> {
    /**
     * 设置最大导入行数。
     *
     * @param maxRows 最大导入行数
     * @return 当前构建器
     */
    ImporterFactoryBuilder<T> maxRows(int maxRows);

    /**
     * 设置导入实体类型。
     *
     * @param entityClass 导入实体类型
     * @return 当前构建器
     */
    ImporterFactoryBuilder<T> entityClass(Class<T> entityClass);

    /**
     * 构建导入器工厂。
     *
     * @return 导入器工厂
     */
    ImporterFactory<T> build();
}
