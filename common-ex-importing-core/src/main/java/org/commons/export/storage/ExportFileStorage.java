package org.commons.export.storage;

import java.io.File;

/**
 * 导出文件存储接口，可实现 OSS、MinIO、本地文件等。
 */
public interface ExportFileStorage {
    StorageResult upload(File file, String objectName, String contentType);
}

