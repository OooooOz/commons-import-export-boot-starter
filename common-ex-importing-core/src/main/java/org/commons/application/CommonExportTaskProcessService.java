package org.commons.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.commons.adapter.dto.ExportTaskPageParamDTO;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.vo.ExportTaskVO;
import org.commons.domain.model.vo.LocalExportFileDownload;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface CommonExportTaskProcessService {

    /**
     * 创建导出任务。
     */
    ExportTaskVO createTask(ExportTaskDTO dto);

    /**
     * 查询导出任务详情。
     */
    ExportTaskVO getTask(Long id);

    /**
     * 分页查询导出任务。
     */
    IPage<ExportTaskProcess> page(ExportTaskPageParamDTO query);

    /**
     * 订阅导出任务状态通知。
     */
    SseEmitter subscribe(String creator);

    /**
     * 解析本地导出文件下载信息。
     */
    LocalExportFileDownload loadLocalFile(String objectName);
}
