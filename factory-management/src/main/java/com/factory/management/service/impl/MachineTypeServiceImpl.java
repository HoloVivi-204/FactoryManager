package com.factory.management.service.impl;

import com.factory.management.dto.request.MachineTypeRequest;
import com.factory.management.dto.request.MachineTypeUpdateRequest;
import com.factory.management.dto.response.MachineTypeResponse;
import com.factory.management.entity.MachineType;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.MachineTypeMapper;
import com.factory.management.repository.MachineTypeRepository;
import com.factory.management.service.MachineTypeService;
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
public class MachineTypeServiceImpl implements MachineTypeService {
    MachineTypeRepository machineTypeRepository;
    MachineTypeMapper machineTypeMapper;

    @Override
    @Transactional
    public MachineTypeResponse create(MachineTypeRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (machineTypeRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.MACHINE_TYPE_CODE_EXISTS);
        }

        MachineType machineType = machineTypeMapper.mapToMachineType(request);
        machineType.setCode(normalizedCode);
        machineType.setName(request.getName().trim());
        machineType.setDescription(trimToNull(request.getDescription()));

        if (machineType.getActive() == null) {
            machineType.setActive(true);
        }

        return machineTypeMapper.mapToMachineTypeResponse(saveMachineType(machineType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineTypeResponse> getAll() {
        return machineTypeRepository.findAllByActiveTrue().stream()
                .map(machineTypeMapper::mapToMachineTypeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MachineTypeResponse getById(Long id) {
        return machineTypeMapper.mapToMachineTypeResponse(findActiveMachineType(id));
    }

    @Override
    @Transactional
    public MachineTypeResponse update(Long id, MachineTypeUpdateRequest request) {
        MachineType machineType = findMachineTypeForUpdate(id);
        String normalizedCode = request.getCode() == null
                ? machineType.getCode()
                : normalizeCode(request.getCode());

        if (machineTypeRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new AppException(ErrorCode.MACHINE_TYPE_CODE_EXISTS);
        }

        machineTypeMapper.updateMachineTypeFromRequest(request, machineType);
        machineType.setCode(normalizedCode);

        if (request.getName() != null) {
            machineType.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            machineType.setDescription(trimToNull(request.getDescription()));
        }

        return machineTypeMapper.mapToMachineTypeResponse(saveMachineType(machineType));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MachineType machineType = findActiveMachineType(id);
        machineType.setActive(false);
    }

    private MachineType findActiveMachineType(Long id) {
        return machineTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_TYPE_ID_NOT_FOUND));
    }

    private MachineType findMachineTypeForUpdate(Long id) {
        return machineTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_TYPE_ID_NOT_FOUND));
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

    private MachineType saveMachineType(MachineType machineType) {
        try {
            return machineTypeRepository.saveAndFlush(machineType);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.MACHINE_TYPE_CODE_EXISTS);
        }
    }
}
