package org.commons.domain.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.entity.ExportTaskProcess;

/**
 * @author
 * @description 针对表【export_task_process(导出任务)】的数据库操作Service
 * @createDate 2025-04-26 20:25:49
 */
public interface ExportTaskProcessService extends IService<ExportTaskProcess> {

    /**
     * 创建导出任务
     *
     * @param dto
     * @return
     */
    Long createTask(ExportTaskDTO dto);
}
