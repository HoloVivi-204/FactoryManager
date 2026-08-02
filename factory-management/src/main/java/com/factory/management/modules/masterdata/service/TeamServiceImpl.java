package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.TeamRequest;
import com.factory.management.modules.masterdata.dto.request.TeamUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.TeamResponse;
import com.factory.management.modules.masterdata.entity.Employee;
import com.factory.management.modules.masterdata.entity.ProductionLine;
import com.factory.management.modules.masterdata.entity.Team;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.modules.masterdata.mapper.TeamMapper;
import com.factory.management.modules.masterdata.repository.EmployeeRepository;
import com.factory.management.modules.masterdata.repository.ProductionLineRepository;
import com.factory.management.modules.masterdata.repository.TeamRepository;
import com.factory.management.modules.masterdata.service.TeamService;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamServiceImpl implements TeamService {
    TeamRepository teamRepository;
    ProductionLineRepository productionLineRepository;
    EmployeeRepository employeeRepository;
    TeamMapper teamMapper;

    @Override
    @Transactional
    public TeamResponse create(TeamRequest request) {
        ProductionLine productionLine = findActiveProductionLine(request.getProductionLineId());
        String normalizedCode = normalizeCode(request.getCode());

        if (teamRepository.existsByProductionLine_IdAndCodeIgnoreCase(
                productionLine.getId(), normalizedCode)) {
            throw new AppException(ErrorCode.TEAM_CODE_EXISTS);
        }

        Team team = teamMapper.mapToTeam(request);
        team.setProductionLine(productionLine);
        applyNormalizedValues(team, request, normalizedCode);

        if (team.getActive() == null) {
            team.setActive(true);
        }

        return teamMapper.mapToTeamResponse(saveTeam(team));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAll() {
        return teamRepository
                .findAllActiveInActiveHierarchy()
                .stream()
                .map(teamMapper::mapToTeamResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllByProductionLineId(Long productionLineId) {
        findActiveProductionLine(productionLineId);
        return teamRepository
                .findAllActiveByProductionLineIdInActiveHierarchy(productionLineId)
                .stream()
                .map(teamMapper::mapToTeamResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeamResponse getById(Long id) {
        return teamMapper.mapToTeamResponse(findActiveTeam(id));
    }

    @Override
    @Transactional
    public TeamResponse update(Long id, TeamUpdateRequest request) {
        Team team = findTeamForUpdate(id);
        ProductionLine productionLine = request.getProductionLineId() == null
                ? team.getProductionLine()
                : findActiveProductionLine(request.getProductionLineId());
        String normalizedCode = request.getCode() == null
                ? team.getCode()
                : normalizeCode(request.getCode());

        if (teamRepository.existsByProductionLine_IdAndCodeIgnoreCaseAndIdNot(
                productionLine.getId(), normalizedCode, id)) {
            throw new AppException(ErrorCode.TEAM_CODE_EXISTS);
        }

        teamMapper.updateTeamFromRequest(request, team);
        team.setProductionLine(productionLine);
        team.setCode(normalizedCode);

        if (request.getName() != null) {
            team.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            team.setDescription(trimToNull(request.getDescription()));
        }

        return teamMapper.mapToTeamResponse(saveTeam(team));
    }

    @Override
    @Transactional
    public TeamResponse assignLeader(Long id, Long employeeId) {
        Team team = findActiveTeam(id);
        Employee employee = employeeRepository
                .findActiveByIdInActiveHierarchy(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));

        if (!employee.getTeam().getId().equals(team.getId())) {
            throw new AppException(ErrorCode.TEAM_LEADER_NOT_MEMBER);
        }

        team.setLeader(employee);
        return teamMapper.mapToTeamResponse(team);
    }

    @Override
    @Transactional
    public TeamResponse removeLeader(Long id) {
        Team team = findActiveTeam(id);
        team.setLeader(null);
        return teamMapper.mapToTeamResponse(team);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Team team = findActiveTeam(id);
        team.setActive(false);
    }

    private ProductionLine findActiveProductionLine(Long id) {
        return productionLineRepository
                .findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_LINE_ID_NOT_FOUND));
    }

    private Team findActiveTeam(Long id) {
        return teamRepository
                .findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_ID_NOT_FOUND));
    }

    private Team findTeamForUpdate(Long id) {
        return teamRepository
                .findByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_ID_NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private void applyNormalizedValues(Team team, TeamRequest request, String normalizedCode) {
        team.setCode(normalizedCode);
        team.setName(request.getName().trim());
        team.setDescription(trimToNull(request.getDescription()));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Team saveTeam(Team team) {
        try {
            return teamRepository.saveAndFlush(team);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.TEAM_CODE_EXISTS);
        }
    }
}
