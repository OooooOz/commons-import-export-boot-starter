package com.commons.exporting.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * core 服务访问配置。
 * <p>
 * starter 通过 HTTP 与独立部署的 core 服务通信，任务创建、状态回写、文件上传均依赖该配置。
 */
@Data
@ConfigurationProperties(prefix = "common.export.core")
public class ExportCoreProperties {
    /**
     * core 服务基础地址。
     */
    private String coreUrl = "http://10.39.11.156:8091";

    /**
     * HTTP 连接超时，单位毫秒。
     */
    private int connectTimeoutMillis = 5000;

    /**
     * HTTP 读取超时，单位毫秒。
     */
    private int readTimeoutMillis = 60000;
}

