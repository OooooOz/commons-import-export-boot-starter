package org.commons.adapter.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.vo.BaseResponse;
import org.commons.domain.model.vo.ExportTaskVO;
import org.commons.domain.service.ExportTaskProcessService;
import org.commons.export.notify.ExportTaskNotifier;
import org.commons.export.storage.LocalExportFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;

/**
 * 导出任务处理控制器
 */
@RestController
@RequestMapping("/api/export/task")
public class ExportTaskProcessController {

    @Autowired
    private ExportTaskProcessService exportTaskProcessService;

    @Autowired
    private ExportTaskNotifier exportTaskNotifier;

    @Autowired(required = false)
    private LocalExportFileStorage localExportFileStorage;

    /**
     * 创建导出任务
     *
     * @param dto 任务信息
     * @return 任务信息
     */
    @PostMapping("/create")
    public BaseResponse<ExportTaskVO> createTask(@RequestBody @Validated ExportTaskDTO dto) {
        ExportTaskVO task = exportTaskProcessService.createTask(dto);
        return BaseResponse.SUCCESS(task);
    }

    /**
     * 查询导出任务详情。
     */
    @GetMapping("/{id}")
    public BaseResponse<ExportTaskVO> getTask(@PathVariable("id") Long id) {
        return BaseResponse.SUCCESS(exportTaskProcessService.getTask(id));
    }

    /**
     * 分页查询导出任务。
     */
    @GetMapping("/page")
    public BaseResponse<IPage<ExportTaskProcess>> page(@RequestParam(value = "businessSystem", required = false) String businessSystem,
                                                       @RequestParam(value = "businessType", required = false) String businessType,
                                                       @RequestParam(value = "creator", required = false) String creator,
                                                       @RequestParam(value = "status", required = false) Integer status,
                                                       @RequestParam(value = "pageNo", defaultValue = "1") long pageNo,
                                                       @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        LambdaQueryWrapper<ExportTaskProcess> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(businessSystem), ExportTaskProcess::getBusinessSystem, businessSystem)
                .eq(StringUtils.hasText(businessType), ExportTaskProcess::getBusinessType, businessType)
                .eq(StringUtils.hasText(creator), ExportTaskProcess::getCreator, creator)
                .eq(status != null, ExportTaskProcess::getStatus, status)
                .orderByDesc(ExportTaskProcess::getId);
        return BaseResponse.SUCCESS(exportTaskProcessService.page(new Page<>(pageNo, pageSize), queryWrapper));
    }

    /**
     * SSE 订阅导出任务状态变化。creator 不传则订阅 anonymous。
     */
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam(value = "creator", required = false) String creator) {
        return exportTaskNotifier.subscribe(creator);
    }

    /**
     * 本地存储模式下载文件；OSS 模式请直接使用 fileUrl。
     */
    @GetMapping("/local-file")
    public void downloadLocalFile(@RequestParam("objectName") String objectName, HttpServletResponse response) throws IOException {
        if (localExportFileStorage == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "当前未启用本地文件存储");
            return;
        }
        File file = localExportFileStorage.getFile(objectName);
        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }
        String fileName = file.getName();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
