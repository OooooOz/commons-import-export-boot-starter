package org.commons.export.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 导出文件存储配置。type=aliyun 时上传 OSS，type=local 时写入本地目录便于开发联调。
 */
@Data
@ConfigurationProperties(prefix = "common.export.storage")
public class ExportStorageProperties {
    /** local 或 aliyun。 */
    private String type = "local";
    /** 对象名前缀。 */
    private String objectPrefix = "exports";

    /** 本地存储根目录。 */
    private String localRoot = "./data/export-files";
    /** 本地文件下载地址前缀。 */
    private String localDownloadUrl = "http://localhost:8091/api/export/task/local-file";

    /** OSS endpoint，例如 oss-cn-hangzhou.aliyuncs.com。 */
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    /** 自定义域名，例如 https://download.example.com；为空时生成 OSS 预签名 URL。 */
    private String customDomain;
    /** 预签名 URL 有效期秒数。 */
    private long urlExpireSeconds = 3600L;
}

