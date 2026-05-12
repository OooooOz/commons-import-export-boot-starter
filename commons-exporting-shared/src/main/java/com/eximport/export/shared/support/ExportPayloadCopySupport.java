package com.eximport.export.shared.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 导出请求载荷深拷贝支持类。
 */
public final class ExportPayloadCopySupport {
    private ExportPayloadCopySupport() {
    }

    public static LinkedHashMap<String, Object> copyMap(Map<String, Object> source) {
        LinkedHashMap<String, Object> target = new LinkedHashMap<>();
        if (source == null || source.isEmpty()) {
            return target;
        }
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            target.put(entry.getKey(), deepCopyValue(entry.getValue()));
        }
        return target;
    }

    public static Object deepCopyValue(Object value) {
        if (value == null) return null;
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character || value instanceof Enum) {
            return value;
        }
        if (value instanceof Date) {
            return new Date(((Date) value).getTime());
        }
        if (value instanceof Map) {
            LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                copy.put(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
            }
            return copy;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<Object> copy = new ArrayList<>(list.size());
            for (Object item : list) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value instanceof Set) {
            Set<?> set = (Set<?>) value;
            Set<Object> copy = new LinkedHashSet<>(set.size());
            for (Object item : set) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            List<Object> copy = new ArrayList<>(collection.size());
            for (Object item : collection) {
                copy.add(deepCopyValue(item));
            }
            return copy;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            Object copy = Array.newInstance(value.getClass().getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, deepCopyValue(Array.get(value, i)));
            }
            return copy;
        }
        return value;
    }
}

