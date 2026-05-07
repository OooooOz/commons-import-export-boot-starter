package org.commons.export.handler;

import org.commons.domain.model.dto.ExportTaskDTO;

import java.util.List;

/**
 * 业务导出处理器。每一种 businessType 实现一个 Handler，异步导出执行器会按 businessType 路由。
 *
 * @param <T> EasyExcel 表头/数据行类型
 */
public interface ExportTaskHandler<T> {
    /**
     * 业务类型，需与 ExportTaskDTO.businessType 一致。
     */
    String businessType();

    /**
     * EasyExcel 表头类。
     */
    Class<T> headClass();

    /**
     * sheet 名称。
     */
    default String sheetName(ExportTaskDTO dto) {
        return "数据";
    }

    /**
     * 默认文件名，不含路径，建议返回 .xlsx 后缀。
     */
    default String fileName(ExportTaskDTO dto) {
        return dto.getBusinessType() + ".xlsx";
    }

    /**
     * 分页查询业务数据。pageNo 从 1 开始；返回空集合表示结束。
     */
    List<T> queryPage(ExportTaskDTO dto, long pageNo, int pageSize);
}

