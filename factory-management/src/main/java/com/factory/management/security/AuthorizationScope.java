package com.factory.management.security;

import com.factory.management.entity.*;
import com.factory.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component("authorizationScope")
@RequiredArgsConstructor
public class AuthorizationScope {
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final MachineRepository machineRepository;
    private final ProductionReportStagingRepository stagingRepository;
    private final ProductionReportRepository reportRepository;
    private final MachineDowntimeStagingRepository downtimeRepository;
    private final QualityReportStagingRepository qualityRepository;
    private final MaterialIssueStagingRepository materialIssueRepository;
    private final EmployeeActualStagingRepository employeeActualRepository;
    private final UserDataScopeRepository userDataScopeRepository;
    private final FactoryRepository factoryRepository;
    private final DepartmentRepository departmentRepository;
    private final ProductionLineRepository productionLineRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public boolean canAccessFinancialScope(Long factoryId, Long departmentId, Long productionLineId) {
        User user = currentUser();
        return canAccessFinancialScope(user, user.getRoles(), factoryId, departmentId, productionLineId);
    }

    /**
     * Evaluates financial access using one role selected by the current UI
     * workspace. This prevents a multi-role account from accidentally using a
     * stronger role while chatting in a more restricted workspace.
     */
    @Transactional(readOnly = true)
    public boolean canAccessFinancialScopeAsRole(
            Role role,
            Long factoryId,
            Long departmentId,
            Long productionLineId
    ) {
        User user = currentUser();
        if (role == null || !has(user, role)) return false;
        return canAccessFinancialScope(user, Set.of(role), factoryId, departmentId, productionLineId);
    }

    private boolean canAccessFinancialScope(
            User user,
            Set<Role> effectiveRoles,
            Long factoryId,
            Long departmentId,
            Long productionLineId
    ) {
        if (hasAny(effectiveRoles, Role.ADMIN, Role.DIRECTOR)) return true;
        if (!hasAny(effectiveRoles, Role.FINANCE, Role.FACTORY_MANAGER)) return false;
        Long resolvedFactory = factoryId;
        Long resolvedDepartment = departmentId;
        if (productionLineId != null) {
            ProductionLine line = productionLineRepository.findById(productionLineId).orElse(null);
            if (line == null) return false;
            resolvedDepartment = line.getDepartment().getId();
            resolvedFactory = line.getDepartment().getFactory().getId();
        } else if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId).orElse(null);
            if (department == null) return false;
            resolvedFactory = department.getFactory().getId();
        } else if (factoryId == null || !factoryRepository.existsById(factoryId)) return false;
        final Long f = resolvedFactory, d = resolvedDepartment;
        var scopes = userDataScopeRepository.findAllByUser_Id(user.getId());
        if (!scopes.isEmpty()) return scopes.stream().anyMatch(scope -> {
            if (scope.getScopeType() == null || scope.getScopeId() == null) return false;
            if (scope.getScopeType() == DataScopeType.FACTORY) {
                return scope.getScopeId().equals(f);
            }
            if (scope.getScopeType() == DataScopeType.DEPARTMENT) {
                return d != null && scope.getScopeId().equals(d);
            }
            if (scope.getScopeType() == DataScopeType.PRODUCTION_LINE) {
                return productionLineId != null && scope.getScopeId().equals(productionLineId);
            }
            return false;
        });
        if (effectiveRoles.contains(Role.FINANCE) && !effectiveRoles.contains(Role.FACTORY_MANAGER)) return false;
        Team own = user.getEmployee().getTeam();
        return own != null && factoryId(own).equals(f);
    }

    /**
     * Returns the active teams visible to the current user under the shared
     * role + data-scope policy. A pure EMPLOYEE has no team-level visibility;
     * personal HR access is handled by {@link #canAccessEmployee(Long)}.
     */
    @Transactional(readOnly = true)
    public Set<Long> accessibleTeamIds() {
        return accessibleTeamIds(currentUser());
    }

    /**
     * Returns visible teams after narrowing authorization to a role selected by
     * the current workspace. Passing a role that is not assigned to the user
     * yields an empty scope.
     */
    @Transactional(readOnly = true)
    public Set<Long> accessibleTeamIdsAsRole(Role role) {
        User user = currentUser();
        if (role == null || !has(user, role)) return Set.of();
        return accessibleTeamIds(user, Set.of(role));
    }

    @Transactional(readOnly = true)
    public Set<Role> currentRoles() {
        return Set.copyOf(currentUser().getRoles());
    }

    @Transactional(readOnly = true)
    public boolean canAccessEmployee(Long employeeId) {
        if (employeeId == null) return false;
        User user = currentUser();
        Employee target = employeeRepository.findById(employeeId).orElse(null);
        if (target == null) return false;
        if (hasAny(user, Role.ADMIN, Role.DIRECTOR)) return true;
        if (!hasAny(user, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
                Role.PRODUCTION_MANAGER, Role.TEAM_LEADER)) {
            return user.getEmployee().getId().equals(employeeId);
        }
        return target.getTeam() != null && accessibleTeamIds(user).contains(target.getTeam().getId());
    }

    @Transactional(readOnly = true)
    public boolean canAccessTeam(Long teamId) {
        User user = currentUser();
        Team target = teamRepository.findById(teamId).orElse(null);
        return target != null && canAccessTeam(user, target);
    }

    @Transactional(readOnly = true)
    public boolean canManageTeam(Long teamId) {
        User user = currentUser();
        if (has(user, Role.ADMIN)) return true;
        if (!hasAny(user, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
                Role.PRODUCTION_MANAGER, Role.TEAM_LEADER)) return false;
        Team target = teamRepository.findById(teamId).orElse(null);
        return target != null && canAccessTeam(user, target);
    }

    @Transactional(readOnly = true)
    public boolean canAccessMachine(Long machineId) {
        Machine machine = machineRepository.findById(machineId).orElse(null);
        return machine != null && canAccessTeam(currentUser(), machine.getTeam());
    }

    @Transactional(readOnly = true)
    public boolean canAccessStaging(Long reportId) {
        ProductionReportStaging report = stagingRepository.findById(reportId).orElse(null);
        return report != null && canAccessTeam(currentUser(), report.getTeam());
    }

    @Transactional(readOnly = true)
    public boolean canManageStaging(Long reportId) {
        User user = currentUser();
        if (!hasAny(user, Role.ADMIN, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
                Role.PRODUCTION_MANAGER, Role.TEAM_LEADER)) return false;
        ProductionReportStaging report = stagingRepository.findById(reportId).orElse(null);
        return report != null && canAccessTeam(user, report.getTeam());
    }

    @Transactional(readOnly = true)
    public boolean canApproveStaging(Long reportId) {
        User user = currentUser();
        // DIRECTOR is an executive read-only role. Approval belongs to the
        // operational management chain; ADMIN remains the emergency override.
        if (has(user, Role.ADMIN)) return true;
        if (!hasAny(user, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
                Role.PRODUCTION_MANAGER)) return false;
        ProductionReportStaging report = stagingRepository.findById(reportId).orElse(null);
        return report != null && canAccessTeam(user, report.getTeam());
    }

    @Transactional(readOnly = true)
    public boolean canAccessReport(Long reportId) {
        ProductionReport report = reportRepository.findById(reportId).orElse(null);
        return report != null && canAccessTeam(currentUser(), report.getTeam());
    }

    @Transactional(readOnly = true)
    public boolean canAccessReportRecord(ProductionReport report) {
        return report != null && canAccessTeam(currentUser(), report.getTeam());
    }

    @Transactional(readOnly = true)
    public boolean canManageDowntime(Long id) {
        return downtimeRepository.findById(id)
                .map(value -> canManageStaging(value.getProductionReportStaging().getId())).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canManageQuality(Long id) {
        return qualityRepository.findById(id)
                .map(value -> canManageStaging(value.getProductionReportStaging().getId())).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canManageMaterialIssue(Long id) {
        return materialIssueRepository.findById(id)
                .map(value -> canManageStaging(value.getProductionReportStaging().getId())).orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canManageEmployeeActual(Long id) {
        return employeeActualRepository.findById(id)
                .map(value -> canManageStaging(value.getProductionReportStaging().getId())).orElse(false);
    }

    private boolean canAccessTeam(User user, Team target) {
        Set<Role> roles = user.getRoles();
        if (roles.contains(Role.ADMIN) || roles.contains(Role.DIRECTOR)) return true;
        if (!hasAny(user, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
                Role.PRODUCTION_MANAGER, Role.TEAM_LEADER, Role.FINANCE)) return false;
        var scopes = userDataScopeRepository.findAllByUser_Id(user.getId());
        if (!scopes.isEmpty()) return scopes.stream().anyMatch(scope -> matchesScope(scope, target));
        if (roles.contains(Role.FINANCE) && !hasAny(user, Role.FACTORY_MANAGER,
                Role.DEPARTMENT_MANAGER, Role.PRODUCTION_MANAGER, Role.TEAM_LEADER)) return false;
        Team own = user.getEmployee().getTeam();
        if (own == null) return false;
        if (roles.contains(Role.FACTORY_MANAGER))
            return factoryId(own).equals(factoryId(target));
        if (roles.contains(Role.DEPARTMENT_MANAGER))
            return departmentId(own).equals(departmentId(target));
        if (roles.contains(Role.PRODUCTION_MANAGER))
            return own.getProductionLine().getId().equals(target.getProductionLine().getId());
        return own.getId().equals(target.getId());
    }

    private Set<Long> accessibleTeamIds(User user) {
        return accessibleTeamIds(user, user.getRoles());
    }

    private Set<Long> accessibleTeamIds(User user, Set<Role> effectiveRoles) {
        List<Team> activeTeams = teamRepository.findAllActiveInActiveHierarchy();
        if (hasAny(effectiveRoles, Role.ADMIN, Role.DIRECTOR)) {
            return activeTeams.stream().map(Team::getId).collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
        if (!hasAny(effectiveRoles, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
                Role.PRODUCTION_MANAGER, Role.TEAM_LEADER, Role.FINANCE)) return Set.of();

        var scopes = userDataScopeRepository.findAllByUser_Id(user.getId());
        if (!scopes.isEmpty()) {
            return activeTeams.stream()
                    .filter(team -> scopes.stream().anyMatch(scope -> matchesScope(scope, team)))
                    .map(Team::getId)
                    .collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
        if (effectiveRoles.contains(Role.FINANCE) && !hasAny(effectiveRoles, Role.FACTORY_MANAGER,
                Role.DEPARTMENT_MANAGER, Role.PRODUCTION_MANAGER, Role.TEAM_LEADER)) return Set.of();

        Team own = user.getEmployee().getTeam();
        if (own == null) return Set.of();
        return activeTeams.stream().filter(team -> {
            if (effectiveRoles.contains(Role.FACTORY_MANAGER)) return factoryId(own).equals(factoryId(team));
            if (effectiveRoles.contains(Role.DEPARTMENT_MANAGER)) return departmentId(own).equals(departmentId(team));
            if (effectiveRoles.contains(Role.PRODUCTION_MANAGER))
                return own.getProductionLine().getId().equals(team.getProductionLine().getId());
            return own.getId().equals(team.getId());
        }).map(Team::getId).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    // Keep this as explicit comparisons: enum switches create a synthetic
    // AuthorizationScope$1 class that can become stale during IDE hot reload.
    private boolean matchesScope(UserDataScope scope, Team target) {
        if (scope.getScopeType() == null || scope.getScopeId() == null) return false;
        if (scope.getScopeType() == DataScopeType.FACTORY) {
            return scope.getScopeId().equals(factoryId(target));
        }
        if (scope.getScopeType() == DataScopeType.DEPARTMENT) {
            return scope.getScopeId().equals(departmentId(target));
        }
        if (scope.getScopeType() == DataScopeType.PRODUCTION_LINE) {
            return scope.getScopeId().equals(target.getProductionLine().getId());
        }
        if (scope.getScopeType() == DataScopeType.TEAM) {
            return scope.getScopeId().equals(target.getId());
        }
        return false;
    }

    private User currentUser() {
        Object authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) return nullUser();
        return userRepository.findByUsernameIgnoreCase(jwt.getToken().getSubject()).orElseGet(this::nullUser);
    }

    private User nullUser() {
        return User.builder().roles(Set.of()).build();
    }

    private boolean has(User user, Role role) { return user.getRoles().contains(role); }
    private boolean hasAny(User user, Role... roles) {
        for (Role role : roles) if (has(user, role)) return true;
        return false;
    }
    private boolean hasAny(Set<Role> assignedRoles, Role... roles) {
        for (Role role : roles) if (assignedRoles.contains(role)) return true;
        return false;
    }
    private Long departmentId(Team team) { return team.getProductionLine().getDepartment().getId(); }
    private Long factoryId(Team team) { return team.getProductionLine().getDepartment().getFactory().getId(); }
}
