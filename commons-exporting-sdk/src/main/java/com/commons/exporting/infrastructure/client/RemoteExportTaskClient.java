package com.commons.exporting.infrastructure.client;

import com.commons.exporting.infrastructure.client.model.BaseResponse;
import com.commons.exporting.infrastructure.client.model.ExportTaskDTO;
import com.commons.exporting.infrastructure.client.model.ExportTaskSuccessDTO;
import com.commons.exporting.infrastructure.client.model.ExportTaskVO;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * starter 侧访问 core 服务的 HTTP 客户端。
 */
public class RemoteExportTaskClient {
    private final ExportTaskCoreFeignClient feignClient;

    public RemoteExportTaskClient(ExportTaskCoreFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    public ExportTaskVO createTask(ExportTaskDTO dto) {
        return unwrap(feignClient.createTask(dto));
    }

    public ExportTaskVO getTask(Long id) {
        return unwrap(feignClient.getTask(id));
    }

    public void markProcessing(Long id) {
        unwrap(feignClient.markProcessing(id, Collections.emptyMap()));
    }

    public void markFailure(Long id, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", StringUtils.hasText(message) ? message : "导出失败");
        unwrap(feignClient.markFailure(id, body));
    }

    public void uploadSuccess(Long id, File file, String fileName) {
        unwrap(feignClient.uploadSuccess(id, file, fileName, "导出完成"));
    }

    public void reportSuccess(Long id, String fileName, String fileUrl) {
        reportSuccess(id, fileName, fileUrl, "导出完成");
    }

    public void reportSuccess(Long id, String fileName, String fileUrl, String message) {
        ExportTaskSuccessDTO dto = new ExportTaskSuccessDTO();
        dto.setFileName(fileName);
        dto.setFileUrl(fileUrl);
        dto.setMessage(StringUtils.hasText(message) ? message : "导出完成");
        unwrap(feignClient.reportSuccess(id, dto));
    }

    private ExportTaskVO unwrap(BaseResponse<ExportTaskVO> body) {
        if (body == null) throw new IllegalStateException("服务无响应");
        if (!body.isSuccess()) throw new IllegalStateException("服务返回失败：" + body.getMessage());
        return body.getData();
    }
}

