package org.commons.exporting.infrastructure.handle;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import org.commons.exporting.domain.model.ExportTaskCreateRequest;

import java.util.List;

/**
 * starter 暴露给业务系统的异步导出处理器。
 * 业务系统实现该接口即可接入异步导出，无需直接感知 core 模块接口。
 *
 * @param <T> Excel 行模型
 */
public interface AsyncExportHandler<T> {

    /**
     * 业务系统标识。
     */
    String businessSystem();

    /**
     * 业务类型标识。
     */
    String businessType();

    /**
     * EasyExcel 表头类。
     */
    Class<T> headClass();

    /**
     * sheet 名称。
     */
    default String sheetName(ExportTaskCreateRequest request) {
        return "数据";
    }

    /**
     * 默认文件名，不含路径。
     */
    default String fileName(ExportTaskCreateRequest request) {
        return request.getBusinessType() + ".xlsx";
    }

    /**
     * 自定义 EasyExcel 写入构建器。
     */
    default void customizeWriter(ExportTaskCreateRequest request, ExcelWriterBuilder writerBuilder) {
        // 默认无扩展
    }

    /**
     * 分页查询业务数据。
     */
    List<T> queryPage(ExportTaskCreateRequest request, long pageNo, int pageSize);
}

