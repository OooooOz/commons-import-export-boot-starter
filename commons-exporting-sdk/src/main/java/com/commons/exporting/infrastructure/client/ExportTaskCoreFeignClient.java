package com.commons.exporting.infrastructure.client;

import com.eximport.export.shared.model.BaseResponse;
import com.eximport.export.shared.model.ExportTaskSuccessDTO;
import com.eximport.export.shared.model.ExportTaskVO;
import com.commons.exporting.infrastructure.client.model.ExportTaskDTO;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.io.File;
import java.util.Map;

/**
 * core 服务导出任务 HTTP 协议定义。
 */
public interface ExportTaskCoreFeignClient {

    @RequestLine("POST /api/export/task/client/create")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    BaseResponse<ExportTaskVO> createTask(ExportTaskDTO dto);

    @RequestLine("GET /api/export/task/{id}")
    @Headers("Accept: application/json")
    BaseResponse<ExportTaskVO> getTask(@Param("id") Long id);

    @RequestLine("POST /api/export/task/client/{id}/processing")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    BaseResponse<ExportTaskVO> markProcessing(@Param("id") Long id, Map<String, Object> body);

    @RequestLine("POST /api/export/task/client/{id}/fail")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    BaseResponse<ExportTaskVO> markFailure(@Param("id") Long id, Map<String, String> body);

    @RequestLine("POST /api/export/task/client/{id}/upload")
    @Headers({"Content-Type: multipart/form-data", "Accept: application/json"})
    BaseResponse<ExportTaskVO> uploadSuccess(@Param("id") Long id,
                                             @Param("file") File file,
                                             @Param("fileName") String fileName,
                                             @Param("message") String message);

    @RequestLine("POST /api/export/task/client/{id}/success")
    @Headers({"Content-Type: application/json", "Accept: application/json"})
    BaseResponse<ExportTaskVO> reportSuccess(@Param("id") Long id, ExportTaskSuccessDTO dto);
}

