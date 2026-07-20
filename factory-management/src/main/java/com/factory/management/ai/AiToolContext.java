package com.factory.management.ai;

import com.factory.management.entity.Role;

import java.time.LocalDate;
import java.util.Set;

public record AiToolContext(
        Long userId,
        Long employeeId,
        String username,
        Set<Role> assignedRoles,
        Set<Role> effectiveRoles,
        Role workspaceRole,
        Set<Long> accessibleTeamIds,
        LocalDate today
) {
}
