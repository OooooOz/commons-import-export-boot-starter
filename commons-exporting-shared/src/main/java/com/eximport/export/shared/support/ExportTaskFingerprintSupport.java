package com.eximport.export.shared.support;

import com.eximport.export.shared.model.ExportTaskFields;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 导出请求指纹支持类。
 */
public final class ExportTaskFingerprintSupport {
    private ExportTaskFingerprintSupport() {
    }

    /**
     * 基于业务侧可见字段构建请求指纹。
     */
    public static String build(ExportTaskFields taskFields) {
        StringBuilder builder = new StringBuilder(256);
        builder.append("businessSystem=").append(normalizeText(taskFields == null ? null : taskFields.getBusinessSystem())).append(';');
        builder.append("businessType=").append(normalizeText(taskFields == null ? null : taskFields.getBusinessType())).append(';');
        builder.append("taskName=").append(normalizeText(taskFields == null ? null : taskFields.getTaskName())).append(';');
        builder.append("fileName=").append(normalizeText(taskFields == null ? null : taskFields.getFileName())).append(';');
        builder.append("creator=").append(normalizeText(taskFields == null ? null : taskFields.getCreator())).append(';');
        builder.append("extMap=");
        appendCanonicalValue(builder, taskFields == null ? null : taskFields.getExtMap());
        return sha256Hex(builder.toString());
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private static void appendCanonicalValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }
        if (value instanceof Map) {
            appendMap(builder, (Map<?, ?>) value);
            return;
        }
        if (value instanceof Collection) {
            appendCollection(builder, (Collection<?>) value);
            return;
        }
        if (value.getClass().isArray()) {
            appendArray(builder, value);
            return;
        }
        if (value instanceof Date) {
            builder.append(((Date) value).getTime());
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            builder.append(String.valueOf(value));
            return;
        }
        builder.append('"').append(escape(String.valueOf(value))).append('"');
    }

    private static void appendMap(StringBuilder builder, Map<?, ?> map) {
        builder.append('{');
        TreeMap<String, Object> sorted = new TreeMap<String, Object>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sorted.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        Iterator<Map.Entry<String, Object>> iterator = sorted.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            builder.append('"').append(escape(entry.getKey())).append('"').append(':');
            appendCanonicalValue(builder, entry.getValue());
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append('}');
    }

    private static void appendCollection(StringBuilder builder, Collection<?> values) {
        builder.append('[');
        Iterator<?> iterator = values.iterator();
        while (iterator.hasNext()) {
            appendCanonicalValue(builder, iterator.next());
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(']');
    }

    private static void appendArray(StringBuilder builder, Object array) {
        builder.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            appendCanonicalValue(builder, Array.get(array, i));
            if (i < length - 1) {
                builder.append(',');
            }
        }
        builder.append(']');
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                String part = Integer.toHexString(item & 0xff);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("生成导出请求指纹失败", e);
        }
    }
}

