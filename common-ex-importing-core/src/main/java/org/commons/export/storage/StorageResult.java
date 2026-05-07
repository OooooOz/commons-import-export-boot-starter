package org.commons.export.storage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageResult {
    private String objectName;
    private String url;
}

