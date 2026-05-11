package org.commons.infrastructure.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.commons.domain.mapper.ExportTaskProcessMapper;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.dto.ExportTaskPageQuery;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.enums.ExportTaskStatusEnum;
import org.commons.domain.model.vo.ExportTaskVO;
import org.commons.domain.service.ExportTaskProcessService;
import org.commons.export.ExportTaskExecutor;
import org.commons.infrastructure.util.ExportTaskNoGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * @author
 * @description 针对表【export_task_process(导出任务)】的数据库操作Service实现
 * @createDate 2025-04-26 20:25:49
 */
@Service
public class ExportTaskProcessServiceImpl extends ServiceImpl<ExportTaskProcessMapper, ExportTaskProcess> implements ExportTaskProcessService {

    @Autowired
    private ExportTaskExecutor exportTaskExecutor;

    @Override
    public ExportTaskVO createTask(ExportTaskDTO dto) {
        ExportTaskProcess entity = BeanUtil.copyProperties(dto, ExportTaskProcess.class);
        entity.setStatus(ExportTaskStatusEnum.INIT.getCode());
        entity.setMessage("任务已创建，等待导出");
        saveWithGeneratedTaskNo(entity);
        exportTaskExecutor.submit(entity.getId(), dto);
        return toVO(this.getById(entity.getId()));
    }

    @Override
    public ExportTaskVO getTask(Long id) {
        return toVO(this.getById(id));
    }

    @Override
    public IPage<ExportTaskProcess> pageQuery(Page<ExportTaskProcess> page, ExportTaskPageQuery query) {
        return baseMapper.selectTaskPage(page, query);
    }

    private ExportTaskVO toVO(ExportTaskProcess entity) {
        if (entity == null) {
            return null;
        }
        return BeanUtil.copyProperties(entity, ExportTaskVO.class);
    }

    private void saveWithGeneratedTaskNo(ExportTaskProcess entity) {
        DuplicateKeyException lastException = null;
        for (int i = 0; i < 5; i++) {
            entity.setTaskNo(ExportTaskNoGenerator.generate());
            try {
                this.save(entity);
                return;
            } catch (DuplicateKeyException e) {
                lastException = e;
            }
        }
        throw new IllegalStateException("生成唯一导出任务号失败，请稍后重试", lastException);
    }
}




