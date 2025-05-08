package org.commons.infrastructure.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.commons.domain.mapper.ExportTaskProcessMapper;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.service.ExportTaskProcessService;
import org.springframework.stereotype.Service;

/**
 * @author
 * @description 针对表【export_task_process(导出任务)】的数据库操作Service实现
 * @createDate 2025-04-26 20:25:49
 */
@Service
public class ExportTaskProcessServiceImpl extends ServiceImpl<ExportTaskProcessMapper, ExportTaskProcess> implements ExportTaskProcessService {

    @Override
    public Long createTask(ExportTaskDTO dto) {
        ExportTaskProcess entity = BeanUtil.copyProperties(dto, ExportTaskProcess.class);
        this.save(entity);
        return entity.getId();
    }
}




