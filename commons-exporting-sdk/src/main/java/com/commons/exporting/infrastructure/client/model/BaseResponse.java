package com.commons.exporting.infrastructure.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * core 服务返回包装。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String message;

    @JsonProperty("success")
    @JsonAlias("isSuccess")
    private boolean success = true;

    private String traceId;
    private T data;
}

