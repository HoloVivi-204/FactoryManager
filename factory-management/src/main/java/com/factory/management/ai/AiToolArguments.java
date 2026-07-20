package com.factory.management.ai;

import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AiToolArguments {
    private AiToolArguments() {
    }

    public static Map<String, Object> objectSchema(Map<String, Object> properties) {
        return objectSchema(properties, List.copyOf(properties.keySet()));
    }

    public static Map<String, Object> objectSchema(
            Map<String, Object> properties,
            List<String> required
    ) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", List.copyOf(required));
        schema.put("additionalProperties", false);
        return schema;
    }

    public static Map<String, Object> nullableString(String description) {
        return Map.of("type", List.of("string", "null"), "description", description);
    }

    public static Map<String, Object> nullableBoolean(String description) {
        return Map.of("type", List.of("boolean", "null"), "description", description);
    }

    public static Map<String, Object> nullableInteger(String description, int minimum, int maximum) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", List.of("integer", "null"));
        value.put("description", description);
        value.put("minimum", minimum);
        value.put("maximum", maximum);
        return value;
    }

    public static Map<String, Object> requiredEnum(String description, List<String> values) {
        return Map.of("type", "string", "description", description, "enum", values);
    }

    public static String text(Map<String, Object> arguments, String key) {
        Object raw = arguments.get(key);
        if (raw == null) return null;
        String value = String.valueOf(raw).trim();
        return value.isEmpty() || "null".equalsIgnoreCase(value) ? null : value;
    }

    public static boolean bool(Map<String, Object> arguments, String key, boolean defaultValue) {
        Object raw = arguments.get(key);
        if (raw == null) return defaultValue;
        if (raw instanceof Boolean value) return value;
        if ("true".equalsIgnoreCase(String.valueOf(raw))) return true;
        if ("false".equalsIgnoreCase(String.valueOf(raw))) return false;
        throw new AppException(ErrorCode.AI_TOOL_ARGUMENT_INVALID);
    }

    public static int integer(Map<String, Object> arguments, String key, int defaultValue, int min, int max) {
        Object raw = arguments.get(key);
        if (raw == null) return defaultValue;
        try {
            int value = raw instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(raw));
            if (value < min || value > max) throw new AppException(ErrorCode.AI_TOOL_ARGUMENT_INVALID);
            return value;
        } catch (NumberFormatException exception) {
            throw new AppException(ErrorCode.AI_TOOL_ARGUMENT_INVALID);
        }
    }

    public static LocalDate date(Map<String, Object> arguments, String key, LocalDate defaultValue) {
        String value = text(arguments, key);
        if (value == null) return defaultValue;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new AppException(ErrorCode.AI_TOOL_ARGUMENT_INVALID);
        }
    }

    public static void validatePeriod(LocalDate from, LocalDate to, int maximumDays) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new AppException(ErrorCode.AI_TOOL_ARGUMENT_INVALID);
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > maximumDays) {
            throw new AppException(ErrorCode.AI_DATE_RANGE_TOO_LARGE);
        }
    }

    public static String normalized(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
