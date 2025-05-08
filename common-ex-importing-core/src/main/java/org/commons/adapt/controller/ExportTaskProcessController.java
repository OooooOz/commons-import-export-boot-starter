package org.commons.adapt.controller;

import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.vo.BaseResponse;
import org.commons.domain.service.ExportTaskProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 导出任务处理控制器
 */
@RestController
@RequestMapping("/api/export/task")
public class ExportTaskProcessController {

    @Autowired
    private ExportTaskProcessService exportTaskProcessService;

    /**
     * 创建导出任务
     *
     * @param dto 任务信息
     * @return 任务id
     */
    @PostMapping("/create")
    public BaseResponse<Long> createTask(@RequestBody @Validated ExportTaskDTO dto) {
        Long taskId = exportTaskProcessService.createTask(dto);
        return BaseResponse.SUCCESS(taskId);
    }
}
