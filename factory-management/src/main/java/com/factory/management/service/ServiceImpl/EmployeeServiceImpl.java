package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.EmployeeRequest;
import com.factory.management.dto.request.EmployeeUpdateRequest;
import com.factory.management.dto.response.EmployeeResponse;
import com.factory.management.entity.Employee;
import com.factory.management.entity.Team;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.EmployeeMapper;
import com.factory.management.repository.EmployeeRepository;
import com.factory.management.repository.TeamRepository;
import com.factory.management.repository.UserRepository;
import com.factory.management.service.Service.EmployeeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {
    EmployeeRepository employeeRepository;
    TeamRepository teamRepository;
    UserRepository userRepository;
    AuditService auditService;
    EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        Team team = request.getTeamId() == null ? null : findActiveTeam(request.getTeamId());
        String normalizedCode = normalizeCode(request.getCode());

        if (employeeRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_EXISTS);
        }

        Employee employee = employeeMapper.mapToEmployee(request);
        employee.setTeam(team);
        employee.setCode(normalizedCode);
        employee.setFullName(request.getFullName().trim());
        employee.setPosition(request.getPosition().trim());

        if (employee.getActive() == null) {
            employee.setActive(true);
        }

        return employeeMapper.mapToEmployeeResponse(saveEmployee(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository
                .findAllByActiveTrue()
                .stream()
                .map(employeeMapper::mapToEmployeeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllByTeamId(Long teamId) {
        findActiveTeam(teamId);
        return employeeRepository
                .findAllByTeam_IdAndActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(teamId)
                .stream()
                .map(employeeMapper::mapToEmployeeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return employeeMapper.mapToEmployeeResponse(findActiveEmployee(id));
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest request) {
        Employee employee = findEmployeeForUpdate(id);
        boolean wasActive = Boolean.TRUE.equals(employee.getActive());
        Team oldTeam = employee.getTeam();
        Team targetTeam = Boolean.TRUE.equals(request.getRemoveFromTeam())
                ? null
                : request.getTeamId() == null ? oldTeam : findActiveTeam(request.getTeamId());
        String normalizedCode = request.getCode() == null
                ? employee.getCode()
                : normalizeCode(request.getCode());

        if (employeeRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_EXISTS);
        }

        employeeMapper.updateEmployeeFromRequest(request, employee);
        employee.setTeam(targetTeam);
        employee.setCode(normalizedCode);

        if (request.getFullName() != null) {
            employee.setFullName(request.getFullName().trim());
        }
        if (request.getPosition() != null) {
            employee.setPosition(request.getPosition().trim());
        }

        boolean movedToAnotherTeam = oldTeam == null ? targetTeam != null
                : targetTeam == null || !oldTeam.getId().equals(targetTeam.getId());
        boolean deactivated = wasActive && Boolean.FALSE.equals(employee.getActive());
        if (movedToAnotherTeam || deactivated) {
            removeLeadership(employee.getId());
        }
        if (deactivated) {
            boolean linkedUserDisabled = disableLinkedUser(employee.getId());
            auditService.record("EMPLOYEE_DEACTIVATED", "EMPLOYEE", employee.getId(),
                    "{\"linkedUserDisabled\":" + linkedUserDisabled + "}");
        }

        return employeeMapper.mapToEmployeeResponse(saveEmployee(employee));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = findActiveEmployee(id);
        removeLeadership(employee.getId());
        employee.setActive(false);
        boolean linkedUserDisabled = disableLinkedUser(employee.getId());
        auditService.record("EMPLOYEE_DEACTIVATED", "EMPLOYEE", employee.getId(),
                "{\"linkedUserDisabled\":" + linkedUserDisabled + "}");
    }

    private Team findActiveTeam(Long id) {
        return teamRepository
                .findByIdAndActiveTrueAndProductionLine_ActiveTrueAndProductionLine_Department_ActiveTrueAndProductionLine_Department_Factory_ActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_ID_NOT_FOUND));
    }

    private Employee findActiveEmployee(Long id) {
        return employeeRepository
                .findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));
    }

    private Employee findEmployeeForUpdate(Long id) {
        return employeeRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));
    }

    private void removeLeadership(Long employeeId) {
        teamRepository.findByLeader_Id(employeeId).ifPresent(team -> team.setLeader(null));
    }

    private boolean disableLinkedUser(Long employeeId) {
        return userRepository.findByEmployee_Id(employeeId).map(user -> {
            user.setEnabled(false);
            long currentVersion = user.getTokenVersion() == null ? 0L : user.getTokenVersion();
            user.setTokenVersion(currentVersion + 1L);
            return true;
        }).orElse(false);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private Employee saveEmployee(Employee employee) {
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.EMPLOYEE_CODE_EXISTS);
        }
    }
}
