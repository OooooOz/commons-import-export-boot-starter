package org.commons.export.handler;

import com.eximport.export.shared.excel.ExcelPageExportHandler;
import com.eximport.export.shared.support.ExportHandlerDescriptor;
import org.commons.domain.model.dto.ExportTaskDTO;

/**
 * 业务导出处理器。每一种 businessSystem + businessType 组合实现一个 Handler，
 * 异步导出执行器会按组合键路由。
 *
 * @param <T> EasyExcel 表头/数据行类型
 */
public interface ExportTaskHandler<T> extends ExcelPageExportHandler<ExportTaskDTO, T>, ExportHandlerDescriptor {
    /**
     * 业务系统，需与 ExportTaskDTO.businessSystem 一致。
     */
    String businessSystem();

    /**
     * 业务类型，需与 ExportTaskDTO.businessType 一致。
     */
    String businessType();

    /**
     * 默认文件名，不含路径，建议返回 .xlsx 后缀。
     */
    default String fileName(ExportTaskDTO dto) {
        return dto.getBusinessType() + ".xlsx";
    }
}

