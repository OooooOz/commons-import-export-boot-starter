package com.eximport.export.shared.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 导出处理器注册与路由键构建支持类。
 */
public final class ExportHandlerRegistrySupport {
    private ExportHandlerRegistrySupport() {
    }

    public static <H extends ExportHandlerDescriptor> Map<String, H> buildHandlerMap(List<H> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, H> mappings = new LinkedHashMap<>();
        for (H handler : handlers) {
            String key = buildHandlerKey(handler.businessSystem(), handler.businessType());
            H existing = mappings.putIfAbsent(key, handler);
            if (existing != null) {
                throw new IllegalStateException("导出处理器重复注册，key=" + key
                        + ", existing=" + existing.getClass().getName()
                        + ", current=" + handler.getClass().getName());
            }
        }
        return Collections.unmodifiableMap(mappings);
    }

    public static String buildHandlerKey(String businessSystem, String businessType) {
        return normalizeKeyPart(businessSystem, "businessSystem")
                + "_"
                + normalizeKeyPart(businessType, "businessType");
    }

    private static String normalizeKeyPart(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        return value.trim();
    }
}

