package org.commons.export.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import org.commons.export.config.ExportStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * Aliyun OSS 存储实现。
 */
@Service
@ConditionalOnProperty(prefix = "common.export.storage", name = "type", havingValue = "aliyun")
public class AliyunOssExportFileStorage implements ExportFileStorage {
    private final ExportStorageProperties properties;
    private final OSS ossClient;

    public AliyunOssExportFileStorage(ExportStorageProperties properties) {
        this.properties = properties;
        if (!StringUtils.hasText(properties.getEndpoint())
                || !StringUtils.hasText(properties.getBucketName())
                || !StringUtils.hasText(properties.getAccessKeyId())
                || !StringUtils.hasText(properties.getAccessKeySecret())) {
            throw new IllegalArgumentException("OSS配置不完整，请配置endpoint、bucketName、accessKeyId、accessKeySecret");
        }
        this.ossClient = new OSSClientBuilder().build(
                properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret());
    }

    @Override
    public StorageResult upload(File file, String objectName, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.length());
        metadata.setContentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream");
        ossClient.putObject(properties.getBucketName(), objectName, file, metadata);
        return new StorageResult(objectName, buildUrl(objectName));
    }

    private String buildUrl(String objectName) {
        if (StringUtils.hasText(properties.getCustomDomain())) {
            String domain = properties.getCustomDomain();
            if (domain.endsWith("/")) {
                domain = domain.substring(0, domain.length() - 1);
            }
            return domain + "/" + objectName;
        }
        Date expiration = new Date(System.currentTimeMillis() + properties.getUrlExpireSeconds() * 1000L);
        URL url = ossClient.generatePresignedUrl(properties.getBucketName(), objectName, expiration);
        return url.toString();
    }

    @PreDestroy
    public void destroy() {
        ossClient.shutdown();
    }
}

