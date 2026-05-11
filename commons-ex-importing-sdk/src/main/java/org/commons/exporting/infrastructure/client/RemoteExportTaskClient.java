package org.commons.exporting.infrastructure.client;

import org.commons.exporting.infrastructure.client.model.BaseResponse;
import org.commons.exporting.infrastructure.client.model.ExportTaskDTO;
import org.commons.exporting.infrastructure.client.model.ExportTaskVO;
import org.springframework.core.io.FileSystemResource;
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

    public ExportTaskVO markProcessing(Long id) {
        return unwrap(feignClient.markProcessing(id, Collections.emptyMap()));
    }

    public ExportTaskVO markFailure(Long id, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("message", StringUtils.hasText(message) ? message : "导出失败");
        return unwrap(feignClient.markFailure(id, body));
    }

    public ExportTaskVO uploadSuccess(Long id, File file, String fileName) {
        return unwrap(feignClient.uploadSuccess(id, new FileSystemResource(file), fileName, "导出完成"));
    }

    private ExportTaskVO unwrap(BaseResponse<ExportTaskVO> body) {
        if (body == null) throw new IllegalStateException("服务无响应");
        if (!body.isSuccess()) throw new IllegalStateException("服务返回失败：" + body.getMessage());
        return body.getData();
    }
}

