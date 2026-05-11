package org.commons.importing;

/**
 * 导入器工厂。
 *
 * @param <T> 导入实体类型
 */
public interface ImporterFactory<T> {
    /**
     * 创建导入器实例。
     *
     * @return 导入器实例
     */
    Importer<T> createImporter();
}
