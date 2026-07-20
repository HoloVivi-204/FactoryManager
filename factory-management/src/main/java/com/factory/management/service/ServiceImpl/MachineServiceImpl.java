package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.MachineRequest;
import com.factory.management.dto.request.MachineUpdateRequest;
import com.factory.management.dto.response.MachineResponse;
import com.factory.management.entity.*;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.MachineMapper;
import com.factory.management.repository.MachineRepository;
import com.factory.management.repository.MachineTypeRepository;
import com.factory.management.repository.TeamRepository;
import com.factory.management.service.Service.MachineService;
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
public class MachineServiceImpl implements MachineService {
    MachineRepository machineRepository;
    MachineTypeRepository machineTypeRepository;
    TeamRepository teamRepository;
    MachineMapper machineMapper;

    @Override
    @Transactional
    public MachineResponse create(MachineRequest request) {
        MachineType machineType = findActiveMachineType(request.getMachineTypeId());
        Team team = findActiveTeam(request.getTeamId());
        String normalizedCode = normalizeCode(request.getCode());

        if (machineRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.MACHINE_CODE_EXISTS);
        }
        String normalizedSerialNumber = trimToNull(request.getSerialNumber());
        if (normalizedSerialNumber != null
                && machineRepository.existsBySerialNumberIgnoreCase(normalizedSerialNumber)) {
            throw new AppException(ErrorCode.MACHINE_SERIAL_NUMBER_EXISTS);
        }

        Machine machine = machineMapper.mapToMachine(request);
        machine.setMachineType(machineType);
        machine.setTeam(team);
        machine.setCode(normalizedCode);
        machine.setName(request.getName().trim());
        machine.setDescription(trimToNull(request.getDescription()));
        machine.setSerialNumber(normalizedSerialNumber);

        if (machine.getOperationalStatus() == null) {
            machine.setOperationalStatus(MachineOperationalStatus.IDLE);
        }
        if (machine.getActive() == null) {
            machine.setActive(true);
        }

        return machineMapper.mapToMachineResponse(saveMachine(machine));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineResponse> getAll() {
        return machineRepository
                .findAllByActiveTrueAndMachineType_ActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue()
                .stream()
                .map(machineMapper::mapToMachineResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineResponse> getAllByTeamId(Long teamId) {
        findActiveTeam(teamId);
        return machineRepository
                .findAllByTeam_IdAndActiveTrueAndMachineType_ActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(teamId)
                .stream()
                .map(machineMapper::mapToMachineResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineResponse> getAllByMachineTypeId(Long machineTypeId) {
        findActiveMachineType(machineTypeId);
        return machineRepository
                .findAllByMachineType_IdAndActiveTrueAndMachineType_ActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(machineTypeId)
                .stream()
                .map(machineMapper::mapToMachineResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineResponse> getAllByOperationalStatus(MachineOperationalStatus operationalStatus) {
        return machineRepository
                .findAllByOperationalStatusAndActiveTrueAndMachineType_ActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(operationalStatus)
                .stream()
                .map(machineMapper::mapToMachineResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MachineResponse getById(Long id) {
        return machineMapper.mapToMachineResponse(findActiveMachine(id));
    }

    @Override
    @Transactional
    public MachineResponse update(Long id, MachineUpdateRequest request) {
        Machine machine = findMachineForUpdate(id);
        MachineType machineType = findActiveMachineType(
                request.getMachineTypeId() == null ? machine.getMachineType().getId() : request.getMachineTypeId()
        );
        Team team = findActiveTeam(request.getTeamId() == null ? machine.getTeam().getId() : request.getTeamId());
        String normalizedCode = request.getCode() == null
                ? machine.getCode()
                : normalizeCode(request.getCode());
        String normalizedSerialNumber = request.getSerialNumber() == null
                ? machine.getSerialNumber()
                : trimToNull(request.getSerialNumber());

        if (machineRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new AppException(ErrorCode.MACHINE_CODE_EXISTS);
        }
        if (normalizedSerialNumber != null
                && machineRepository.existsBySerialNumberIgnoreCaseAndIdNot(normalizedSerialNumber, id)) {
            throw new AppException(ErrorCode.MACHINE_SERIAL_NUMBER_EXISTS);
        }

        machineMapper.updateMachineFromRequest(request, machine);
        machine.setMachineType(machineType);
        machine.setTeam(team);
        machine.setCode(normalizedCode);
        machine.setSerialNumber(normalizedSerialNumber);

        if (request.getName() != null) {
            machine.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            machine.setDescription(trimToNull(request.getDescription()));
        }

        return machineMapper.mapToMachineResponse(saveMachine(machine));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Machine machine = findActiveMachine(id);
        machine.setActive(false);
    }

    private MachineType findActiveMachineType(Long id) {
        return machineTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_TYPE_ID_NOT_FOUND));
    }

    private Team findActiveTeam(Long id) {
        return teamRepository
                .findByIdAndActiveTrueAndProductionLine_ActiveTrueAndProductionLine_Department_ActiveTrueAndProductionLine_Department_Factory_ActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_ID_NOT_FOUND));
    }

    private Machine findActiveMachine(Long id) {
        return machineRepository
                .findByIdAndActiveTrueAndMachineType_ActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_ID_NOT_FOUND));
    }

    private Machine findMachineForUpdate(Long id) {
        return machineRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_ID_NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Machine saveMachine(Machine machine) {
        try {
            return machineRepository.saveAndFlush(machine);
        } catch (DataIntegrityViolationException exception) {
            if (exception.getMostSpecificCause().getMessage().contains("serial_number")) {
                throw new AppException(ErrorCode.MACHINE_SERIAL_NUMBER_EXISTS);
            }
            throw new AppException(ErrorCode.MACHINE_CODE_EXISTS);
        }
    }
}
