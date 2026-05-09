package org.commons.exporting.domain.service;

import org.commons.exporting.domain.model.ExportTaskCreateRequest;
import org.commons.exporting.domain.model.ExportTaskInfo;

/**
 * 异步导出 SDK 门面，业务系统只需依赖 starter 并注入该接口即可创建/查询导出任务。
 */
public interface Exporting {

    /**
     * 创建异步导出任务。
     */
    ExportTaskInfo createTask(ExportTaskCreateRequest request);

    /**
     * 查询导出任务详情。
     */
    ExportTaskInfo getTask(Long id);
}

