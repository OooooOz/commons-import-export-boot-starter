package com.commons.exporting.domain.service;


import com.commons.exporting.domain.model.ExportTaskCreateRequest;
import com.commons.exporting.domain.model.ExportTaskInfo;

/**
 * 异步导出 SDK 门面。
 * <p>
 * 业务系统只需引入 starter 并注入该接口，即可完成导出任务的创建与查询。
 * 导出任务元数据统一由 core 服务持久化，真正的业务数据查询与 Excel 生成则在业务系统本地异步执行。
 */
public interface Exporting {

    /**
     * 创建异步导出任务并提交本地异步执行。
     * <p>
     * 调用成功后会先由 core 服务创建任务记录并返回任务信息，随后 starter 在本地线程池中匹配
     * {@code AsyncExportHandler} 执行实际导出流程。
     *
     * @param request 导出任务创建参数；允许为空，空值时会按默认值创建任务
     * @return 导出任务信息，包含 core 生成的任务标识与任务号
     */
    ExportTaskInfo createTask(ExportTaskCreateRequest request);

    /**
     * 按任务主键查询导出任务详情。
     *
     * @param id core 侧导出任务主键
     * @return 当前任务的最新快照；若任务不存在则返回字段为空的结果对象
     */
    ExportTaskInfo getTask(Long id);
}

