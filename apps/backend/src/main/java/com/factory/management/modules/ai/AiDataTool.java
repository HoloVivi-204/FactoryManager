package com.factory.management.modules.ai;

import com.factory.management.modules.auth.entity.Role;
import java.util.Map;
import java.util.Set;

public interface AiDataTool {
    String name();
    String description();
    Map<String, Object> parametersSchema();
    Set<Role> allowedRoles();
    AiToolResult execute(Map<String, Object> arguments, AiToolContext context);

    default boolean isAllowed(AiToolContext context) {
        return context.effectiveRoles().stream().anyMatch(allowedRoles()::contains);
    }

    default Map<String, Object> modelDefinition() {
        return Map.of(
                "type", "function",
                "name", name(),
                "description", description(),
                "parameters", parametersSchema(),
                "strict", true
        );
    }
}
