package org.commons.adapter.api;

import org.commons.adapter.dto.ExportTaskFailureDTO;
import org.commons.adapter.dto.ExportTaskSuccessDTO;
import org.commons.application.CommonExportTaskProcessService;
import org.commons.domain.model.dto.ExportTaskDTO;
import org.commons.domain.model.vo.BaseResponse;
import org.commons.domain.model.vo.ExportTaskVO;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 导出任务处理控制器
 */
@RestController
@RequestMapping("/api/export/task/client")
public class ExportTaskProcessClientController {

    private final CommonExportTaskProcessService commonExportTaskProcessService;

    public ExportTaskProcessClientController(CommonExportTaskProcessService commonExportTaskProcessService) {
        this.commonExportTaskProcessService = commonExportTaskProcessService;
    }

    /**
     * sdk 客户端模式创建导出任务：任务只存储在 core，不由 core 执行业务数据查询。
     */
    @PostMapping("/create")
    public BaseResponse<ExportTaskVO> createClientTask(@RequestBody @Validated ExportTaskDTO dto) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.createClientTask(dto));
    }

    /**
     * sdk 回写处理中状态。
     */
    @PostMapping("/{id}/processing")
    public BaseResponse<ExportTaskVO> markProcessing(@PathVariable("id") Long id) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.markProcessing(id));
    }

    /**
     * sdk 上传导出文件，core 统一存储文件并将任务标记为成功。
     */
    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
     * sdk 或业务系统回写成功状态：文件已由业务系统上传到自身存储，仅回写文件地址。
     */
    @PostMapping("/{id}/success")
    public BaseResponse<ExportTaskVO> reportSuccess(@PathVariable("id") Long id,
                                                    @RequestBody @Validated ExportTaskSuccessDTO dto) {
        return BaseResponse.SUCCESS(commonExportTaskProcessService.reportSuccess(id, dto.getFileName(), dto.getFileUrl(), dto.getMessage()));
    }

    /**
     * sdk 回写失败状态。
     */
    @PostMapping("/{id}/fail")
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
}
