package com.factory.management.modules.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class AiPayloads {

    private AiPayloads() {}

    static Map<String, Object> objectMap(Object value) {
        if (!(value instanceof Map<?, ?> source)) {
            return Map.of();
        }
        return copyObjectMap(source);
    }

    static Map<String, Object> copyObjectMap(Map<?, ?> source) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() instanceof String key) {
                copy.put(key, entry.getValue());
            }
        }
        return copy;
    }

    static List<Map<String, Object>> objectMaps(Object value) {
        return objectMaps(value, Integer.MAX_VALUE);
    }

    static List<Map<String, Object>> objectMaps(Object value, int limit) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Object item : values) {
            if (rows.size() >= limit) {
                break;
            }
            if (item instanceof Map<?, ?> map) {
                rows.add(copyObjectMap(map));
            }
        }
        return List.copyOf(rows);
    }
}
