package com.factory.management.modules.ai;

import java.util.List;
import java.util.Map;

public record AiModelTurn(
        String directAnswer,
        String model,
        List<Map<String, Object>> output,
        List<ToolCall> toolCalls
) {
    public record ToolCall(String callId, String name, Map<String, Object> arguments) {
    }
}
