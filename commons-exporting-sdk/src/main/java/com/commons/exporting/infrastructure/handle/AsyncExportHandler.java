package com.commons.exporting.infrastructure.handle;

import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.eximport.export.shared.excel.ExcelPageExportHandler;
import com.eximport.export.shared.support.ExportHandlerDescriptor;

/**
 * 业务系统实现的异步导出处理器。
 * <p>
 * starter 会根据 {@link #businessSystem()} 与 {@link #businessType()} 的组合唯一匹配处理器，
 * 再通过继承的分页查询能力拉取业务数据并生成 Excel。
 * 业务系统只需要实现该接口，不需要直接依赖或调用 core 模块接口。
 *
 * @param <T> Excel 行模型
 */
public interface AsyncExportHandler<T> extends ExcelPageExportHandler<ExportTaskCreateRequest, T>, ExportHandlerDescriptor {

    /**
     * 返回当前处理器所属的业务系统标识。
     *
     * @return 业务系统唯一标识，不能为空
     */
    String businessSystem();

    /**
     * 返回当前处理器负责的业务类型标识。
     *
     * @return 业务类型唯一标识，不能为空
     */
    String businessType();

    /**
     * 生成默认文件名，不含路径。
     * <p>
     * 当调用方未显式传入文件名时会回退到该默认值。
     *
     * @param request 导出请求
     * @return 默认文件名，允许不带扩展名，starter 会自动补齐为 {@code .xlsx}
     */
    default String fileName(ExportTaskCreateRequest request) {
        return request.getBusinessType() + ".xlsx";
    }
}

