package com.factory.management.modules.ai;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AiToolResult(
        String toolName,
        String dataStatus,
        Map<String, Object> data,
        List<Source> sources,
        List<String> warnings,
        AiDashboard dashboard
) {
    public AiToolResult(
            String toolName,
            String dataStatus,
            Map<String, Object> data,
            List<Source> sources,
            List<String> warnings
    ) {
        this(toolName, dataStatus, data, sources, warnings, null);
    }

    public record Source(
            String type,
            String label,
            String dataStatus,
            int recordCount,
            LocalDateTime asOf
    ) {
    }
}
