package org.commons.export.storage;

import org.commons.export.config.ExportStorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地存储实现，适合开发/测试环境；生产可配置 common.export.storage.type=aliyun。
 */
@Service
@ConditionalOnProperty(prefix = "common.export.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalExportFileStorage implements ExportFileStorage {
    private final ExportStorageProperties properties;

    public LocalExportFileStorage(ExportStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StorageResult upload(File file, String objectName, String contentType) {
        try {
            Path root = Paths.get(properties.getLocalRoot()).toAbsolutePath().normalize();
            Path target = root.resolve(objectName).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("非法文件路径");
            }
            Files.createDirectories(target.getParent());
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            String url = buildDownloadUrl(objectName);
            return new StorageResult(objectName, url);
        } catch (IOException e) {
            throw new RuntimeException("保存本地导出文件失败", e);
        }
    }

    public File getFile(String objectName) {
        Path root = Paths.get(properties.getLocalRoot()).toAbsolutePath().normalize();
        Path target = root.resolve(objectName).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("非法文件路径");
        }
        return target.toFile();
    }

    private String buildDownloadUrl(String objectName) {
        String baseUrl = properties.getLocalDownloadUrl();
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = "/api/export/task/local-file";
        }
        try {
            return baseUrl + "?objectName=" + URLEncoder.encode(objectName, "UTF-8");
        } catch (Exception e) {
            return baseUrl + "?objectName=" + objectName;
        }
    }
}

