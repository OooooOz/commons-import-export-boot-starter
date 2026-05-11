package org.commons.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.commons.adapter.dto.ExportTaskPageParamDTO;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.vo.ExportTaskVO;
import org.commons.domain.model.vo.LocalExportFileDownload;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;

public interface CommonExportTaskProcessService {

    /**
     * 创建导出任务。
     */
    ExportTaskVO createTask(ExportTaskDTO dto);

    /**
     * 仅创建导出任务，不在 core 侧提交执行；供业务系统 starter 远程创建任务使用。
     */
    ExportTaskVO createClientTask(ExportTaskDTO dto);

    /**
     * 将任务标记为处理中。
     */
    ExportTaskVO markProcessing(Long id);

    /**
     * 上传导出文件并将任务标记为成功。
     */
    ExportTaskVO uploadSuccess(Long id, File file, String fileName, String message);

    /**
     * 将任务标记为失败。
     */
    ExportTaskVO markFailure(Long id, String message);

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
