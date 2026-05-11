package org.commons.adapter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.commons.adapter.dto.ExportTaskFailureDTO;
import org.commons.adapter.dto.ExportTaskPageParamDTO;
import org.commons.application.CommonExportTaskProcessService;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.entity.ExportTaskProcess;
import org.commons.domain.model.vo.BaseResponse;
import org.commons.domain.model.vo.ExportTaskVO;
import org.commons.domain.model.vo.LocalExportFileDownload;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 导出任务处理控制器
 */
@RestController
@RequestMapping("/api/export/task")
public class ExportTaskProcessController {

    private final CommonExportTaskProcessService commonExportTaskProcessService;

    public ExportTaskProcessController(CommonExportTaskProcessService commonExportTaskProcessService) {
        this.commonExportTaskProcessService = commonExportTaskProcessService;
    }

    /**
     * 创建导出任务
     *
     * @param dto 任务信息
     * @return 任务信息
     */
    @PostMapping("/create")
    public BaseResponse<ExportTaskVO> createTask(@RequestBody @Validated ExportTaskDTO dto) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.createTask(dto));
    }

    /**
     * starter 客户端模式创建导出任务：任务只存储在 core，不由 core 执行业务数据查询。
     */
    @PostMapping("/client/create")
    public BaseResponse<ExportTaskVO> createClientTask(@RequestBody @Validated ExportTaskDTO dto) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.createClientTask(dto));
    }

    /**
     * starter 回写处理中状态。
     */
    @PostMapping("/client/{id}/processing")
    public BaseResponse<ExportTaskVO> markProcessing(@PathVariable("id") Long id) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.markProcessing(id));
    }

    /**
     * starter 上传导出文件，core 统一存储文件并将任务标记为成功。
     */
    @PostMapping(value = "/client/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<ExportTaskVO> uploadSuccess(@PathVariable("id") Long id,
                                                    @RequestPart("file") MultipartFile file,
                                                    @RequestParam(value = "fileName", required = false) String fileName,
                                                    @RequestParam(value = "message", required = false) String message) throws IOException {
        Path tempFile = Files.createTempFile("client-export-" + id + "-", ".xlsx");
        try {
            file.transferTo(tempFile.toFile());
            String targetFileName = fileName != null ? fileName : file.getOriginalFilename();
            return BaseResponse.SUCCESS(commonExportTaskProcessService.uploadSuccess(id, tempFile.toFile(), targetFileName, message));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * starter 回写失败状态。
     */
    @PostMapping("/client/{id}/fail")
    public BaseResponse<ExportTaskVO> markFailure(@PathVariable("id") Long id,
                                                  @RequestBody(required = false) ExportTaskFailureDTO dto) {
        String message = dto == null ? null : dto.getMessage();
        return BaseResponse.SUCCESS(commonExportTaskProcessService.markFailure(id, message));
    }

    /**
     * 查询导出任务详情。
     */
    @GetMapping("/{id}")
    public BaseResponse<ExportTaskVO> getTask(@PathVariable("id") Long id) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.getTask(id));
    }

    /**
     * 分页查询导出任务。
     */
    @GetMapping("/page")
    public BaseResponse<IPage<ExportTaskProcess>> page(@Validated ExportTaskPageParamDTO query) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.page(query));
    }

    /**
     * SSE 订阅导出任务状态变化。creator 不传则订阅 anonymous。
     */
    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam(value = "creator", required = false) String creator) {
        return commonExportTaskProcessService.subscribe(creator);
    }

    /**
     * 本地存储模式下载文件；OSS 模式请直接使用 fileUrl。
     */
    @GetMapping("/local-file")
    public void downloadLocalFile(@RequestParam("objectName") String objectName, HttpServletResponse response) throws IOException {
        LocalExportFileDownload download = commonExportTaskProcessService.loadLocalFile(objectName);
        if (!download.downloadable()) {
            response.sendError(download.getStatusCode(), download.getMessage());
            return;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(download.getFileName(), StandardCharsets.UTF_8.name()));
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        Files.copy(download.getFile().toPath(), response.getOutputStream());
    }
}
