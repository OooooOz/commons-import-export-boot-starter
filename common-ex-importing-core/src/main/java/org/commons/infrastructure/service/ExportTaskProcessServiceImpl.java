package org.commons.infrastructure.service;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eximport.export.shared.model.ExportTaskVO;
import org.commons.domain.mapper.ExportTaskProcessMapper;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.dto.ExportTaskPageQuery;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.enums.ExportTaskStatusEnum;
import org.commons.domain.service.ExportTaskProcessService;
import org.commons.export.ExportTaskExecutor;
import org.commons.infrastructure.util.ExportTaskNoGenerator;
import org.commons.infrastructure.util.ExportTaskRequestDedupLockManager;
import org.commons.infrastructure.util.ExportTaskRequestFingerprintUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @description 针对表【export_task_process(导出任务)】的数据库操作Service实现
 * @createDate 2025-04-26 20:25:49
 */
@Service
public class ExportTaskProcessServiceImpl extends ServiceImpl<ExportTaskProcessMapper, ExportTaskProcess> implements ExportTaskProcessService {

    private static final List<Integer> REUSABLE_STATUSES = Arrays.asList(
            ExportTaskStatusEnum.INIT.getCode(),
            ExportTaskStatusEnum.PROCESSING.getCode());

    private final ExportTaskExecutor exportTaskExecutor;
    private final ExportTaskRequestDedupLockManager requestDedupLockManager;

    public ExportTaskProcessServiceImpl(ExportTaskExecutor exportTaskExecutor,
                                        ExportTaskRequestDedupLockManager requestDedupLockManager) {
        this.exportTaskExecutor = exportTaskExecutor;
        this.requestDedupLockManager = requestDedupLockManager;
    }

    @Override
    public ExportTaskVO createTask(ExportTaskDTO dto) {
        String requestFingerprint = ExportTaskRequestFingerprintUtil.build(dto);
        return requestDedupLockManager.execute(requestFingerprint, () -> {
            ExportTaskProcess existing = findReusableTask(requestFingerprint);
            if (existing != null) return toVO(existing, false);
            ExportTaskProcess entity = BeanUtil.copyProperties(dto, ExportTaskProcess.class);
            entity.setRequestFingerprint(requestFingerprint);
            entity.setStatus(ExportTaskStatusEnum.INIT.getCode());
            entity.setMessage("任务已创建，等待导出");
            saveWithGeneratedTaskNo(entity);
            exportTaskExecutor.submit(entity.getId(), dto);
            return toVO(this.getById(entity.getId()), true);
        });
    }

    @Override
    public ExportTaskProcess findReusableTask(String requestFingerprint) {
        if (requestFingerprint == null || requestFingerprint.trim().isEmpty()) return null;
        return baseMapper.selectLatestReusableTask(requestFingerprint, REUSABLE_STATUSES);
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
        return toVO(entity, null);
    }

    private ExportTaskVO toVO(ExportTaskProcess entity, Boolean submitRequired) {
        if (entity == null) return null;
        ExportTaskVO vo = BeanUtil.copyProperties(entity, ExportTaskVO.class);
        vo.setSubmitRequired(submitRequired);
        return vo;
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




