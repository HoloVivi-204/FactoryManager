package com.factory.management.ai;

import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiToolRegistry {
    private final List<AiDataTool> tools;

    public List<Map<String, Object>> definitions(AiToolContext context) {
        return tools.stream()
                .filter(tool -> tool.isAllowed(context))
                .sorted(java.util.Comparator.comparing(AiDataTool::name))
                .map(AiDataTool::modelDefinition)
                .toList();
    }

    public AiToolResult execute(String name, Map<String, Object> arguments, AiToolContext context) {
        AiDataTool tool = tools.stream().filter(candidate -> candidate.name().equals(name)).findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.AI_TOOL_NOT_ALLOWED));
        if (!tool.isAllowed(context)) throw new AppException(ErrorCode.AI_TOOL_NOT_ALLOWED);
        Map<String, Object> safeArguments = arguments == null
                ? Map.of()
                : new LinkedHashMap<>(arguments);
        try {
            return tool.execute(safeArguments, context);
        } catch (AppException exception) {
            if (exception.getErrorCode() == ErrorCode.AI_TOOL_ARGUMENT_INVALID) {
                log.warn("AI Tool {} rejected routing arguments: {}", name, diagnostic(safeArguments));
            }
            throw exception;
        }
    }

    private Map<String, Object> diagnostic(Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : List.of(
                "detailType", "fromDate", "toDate", "attendanceStatus",
                "unplannedOnly", "includeTemporary", "limit",
                "currentFrom", "currentTo", "previousFrom", "previousTo", "groupBy")) {
            Object value = arguments.get(key);
            if (value != null) result.put(key, value + " (" + value.getClass().getSimpleName() + ")");
        }
        return result;
    }
}
