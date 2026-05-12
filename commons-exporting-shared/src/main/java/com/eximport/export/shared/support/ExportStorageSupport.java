package com.eximport.export.shared.support;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 导出存储路径与内容类型支持类。
 */
public final class ExportStorageSupport {
    public static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExportStorageSupport() {
    }

    public static String buildObjectName(String objectPrefix, String taskNo, String fileName) {
        return buildObjectName(objectPrefix, taskNo, fileName, new Date());
    }

    public static String buildObjectName(String objectPrefix, String taskNo, String fileName, Date date) {
        String actualDate = new SimpleDateFormat("yyyyMMdd").format(date == null ? new Date() : date);
        String prefix = (objectPrefix == null || objectPrefix.trim().isEmpty()) ? "exports" : trimSlash(objectPrefix);
        return prefix + "/" + actualDate + "/" + taskNo + "-" + fileName;
    }

    public static String trimSlash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String result = value.trim();
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}

