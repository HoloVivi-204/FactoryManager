package com.factory.management.modules.ai;

import com.factory.management.modules.auth.entity.Role;
import com.factory.management.modules.auth.entity.User;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.common.security.AuthorizationScope;
import com.factory.management.common.security.CurrentUserService;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiScopeContextService {
    private final CurrentUserService currentUserService;
    private final AuthorizationScope authorizationScope;

    @Transactional(readOnly = true)
    public AiToolContext current(String workspaceRole) {
        User user = currentUserService.user();
        Set<Role> assignedRoles = Set.copyOf(user.getRoles());
        Role selectedRole = parseRole(workspaceRole);
        if (selectedRole != null && !assignedRoles.contains(selectedRole)) {
            throw new AppException(ErrorCode.AI_WORKSPACE_ROLE_INVALID);
        }
        Set<Role> effectiveRoles = selectedRole == null ? assignedRoles : Set.of(selectedRole);
        Set<Long> teams = selectedRole == null
                ? authorizationScope.accessibleTeamIds()
                : authorizationScope.accessibleTeamIdsAsRole(selectedRole);
        return new AiToolContext(
                user.getId(),
                user.getEmployee().getId(),
                user.getUsername(),
                assignedRoles,
                effectiveRoles,
                selectedRole,
                Set.copyOf(teams),
                LocalDate.now()
        );
    }

    private Role parseRole(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.startsWith("ROLE_")) value = value.substring(5);
        try {
            return Role.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.AI_WORKSPACE_ROLE_INVALID);
        }
    }
}
