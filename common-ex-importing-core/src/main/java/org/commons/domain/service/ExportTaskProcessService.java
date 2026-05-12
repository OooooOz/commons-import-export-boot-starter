package org.commons.domain.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.eximport.export.shared.model.ExportTaskVO;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.dto.ExportTaskPageQuery;
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
    ExportTaskVO createTask(ExportTaskDTO dto);

    /**
     * 按请求指纹查询可复用的已有任务。
     * <p>
     * 仅复用 INIT / PROCESSING / SUCCESS 状态任务；FAIL 状态允许重新创建。
     */
    ExportTaskProcess findReusableTask(String requestFingerprint);

    /**
     * 查询导出任务详情
     *
     * @param id 任务id
     * @return 任务详情
     */
    ExportTaskVO getTask(Long id);

    /**
     * 分页查询导出任务。
     *
     * @param page 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ExportTaskProcess> pageQuery(Page<ExportTaskProcess> page, ExportTaskPageQuery query);
}
