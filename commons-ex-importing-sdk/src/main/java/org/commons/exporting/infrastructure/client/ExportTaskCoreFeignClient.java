package org.commons.exporting.infrastructure.client;

import org.commons.exporting.infrastructure.client.model.BaseResponse;
import org.commons.exporting.infrastructure.client.model.ExportTaskDTO;
import org.commons.exporting.infrastructure.client.model.ExportTaskVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * core 服务导出任务 Feign 客户端。
 */
@FeignClient(
        name = "commonExportTaskCoreClient",
        url = "${common.export.core.core-url:http://localhost:8091}",
        configuration = ExportTaskCoreFeignConfiguration.class
)
public interface ExportTaskCoreFeignClient {

    @PostMapping("/api/export/task/client/create")
    BaseResponse<ExportTaskVO> createTask(@RequestBody ExportTaskDTO dto);

    @GetMapping("/api/export/task/{id}")
    BaseResponse<ExportTaskVO> getTask(@PathVariable("id") Long id);

    @PostMapping("/api/export/task/client/{id}/processing")
    BaseResponse<ExportTaskVO> markProcessing(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    @PostMapping("/api/export/task/client/{id}/fail")
    BaseResponse<ExportTaskVO> markFailure(@PathVariable("id") Long id, @RequestBody Map<String, String> body);

    @PostMapping(value = "/api/export/task/client/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    BaseResponse<ExportTaskVO> uploadSuccess(@PathVariable("id") Long id,
                                             @RequestPart("file") Resource file,
                                             @RequestParam("fileName") String fileName,
                                             @RequestParam("message") String message);
}

