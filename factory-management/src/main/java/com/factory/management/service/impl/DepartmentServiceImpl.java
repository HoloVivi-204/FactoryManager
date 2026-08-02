package com.factory.management.service.impl;

import com.factory.management.dto.request.DepartmentRequest;
import com.factory.management.dto.request.DepartmentUpdateRequest;
import com.factory.management.dto.response.DepartmentResponse;
import com.factory.management.dto.response.DepartmentTypeResponse;
import com.factory.management.entity.Department;
import com.factory.management.entity.DepartmentType;
import com.factory.management.entity.Factory;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.DepartmentMapper;
import com.factory.management.repository.DepartmentRepository;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.service.DepartmentService;
import java.util.Arrays;
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
public class DepartmentServiceImpl implements DepartmentService {
    DepartmentRepository departmentRepository;
    FactoryRepository factoryRepository;
    DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        Factory factory = findActiveFactory(request.getFactoryId());
        ensureDepartmentTypeAvailable(factory.getId(), request.getDepartmentType(), null);

        Department department = Department.builder()
                .factory(factory)
                .departmentType(request.getDepartmentType())
                .active(request.getActive() == null || request.getActive())
                .build();
        applyGeneratedInformation(
                department,
                factory,
                request.getDepartmentType(),
                request.getDescription(),
                true
        );

        return departmentMapper.mapToDepartmentResponse(saveDepartment(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAllActiveInActiveHierarchy().stream()
                .map(departmentMapper::mapToDepartmentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllByFactoryId(Long factoryId) {
        findActiveFactory(factoryId);
        return departmentRepository.findAllActiveByFactoryIdInActiveHierarchy(factoryId).stream()
                .map(departmentMapper::mapToDepartmentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentTypeResponse> getTypes() {
        return Arrays.stream(DepartmentType.values())
                .map(type -> DepartmentTypeResponse.builder()
                        .type(type)
                        .codeSuffix(type.getCodeSuffix())
                        .name(type.getDisplayName())
                        .description(type.getDefaultDescription())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return departmentMapper.mapToDepartmentResponse(findActiveDepartment(id));
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentUpdateRequest request) {
        Department department = findDepartmentInActiveFactory(id);
        Factory factory = request.getFactoryId() == null
                ? department.getFactory()
                : findActiveFactory(request.getFactoryId());
        DepartmentType departmentType = request.getDepartmentType() == null
                ? department.getDepartmentType()
                : request.getDepartmentType();
        boolean typeChanged = department.getDepartmentType() != departmentType;

        ensureDepartmentTypeAvailable(factory.getId(), departmentType, id);
        applyGeneratedInformation(
                department,
                factory,
                departmentType,
                request.getDescription(),
                typeChanged
        );
        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }

        return departmentMapper.mapToDepartmentResponse(saveDepartment(department));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = findActiveDepartment(id);
        department.setActive(false);
    }

    private Factory findActiveFactory(Long id) {
        return factoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND));
    }

    private Department findActiveDepartment(Long id) {
        return departmentRepository.findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_ID_NOT_FOUND));
    }

    private Department findDepartmentInActiveFactory(Long id) {
        return departmentRepository.findByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_ID_NOT_FOUND));
    }

    private void ensureDepartmentTypeAvailable(Long factoryId, DepartmentType departmentType, Long currentId) {
        boolean exists = currentId == null
                ? departmentRepository.existsByFactory_IdAndDepartmentType(factoryId, departmentType)
                : departmentRepository.existsByFactory_IdAndDepartmentTypeAndIdNot(
                        factoryId,
                        departmentType,
                        currentId
                );
        if (exists) {
            throw new AppException(ErrorCode.DEPARTMENT_TYPE_EXISTS);
        }
    }

    private void applyGeneratedInformation(
            Department department,
            Factory factory,
            DepartmentType departmentType,
            String requestedDescription,
            boolean useDefaultDescription
    ) {
        department.setFactory(factory);
        department.setDepartmentType(departmentType);
        department.setCode(generateCode(factory, departmentType));
        department.setName(departmentType.getDisplayName());

        if (requestedDescription != null) {
            department.setDescription(descriptionOrDefault(requestedDescription, departmentType));
        } else if (useDefaultDescription || department.getDescription() == null) {
            department.setDescription(departmentType.getDefaultDescription());
        }
    }

    private String generateCode(Factory factory, DepartmentType departmentType) {
        return factory.getCode().trim().toUpperCase(Locale.ROOT)
                + "-"
                + departmentType.getCodeSuffix();
    }

    private String descriptionOrDefault(String description, DepartmentType departmentType) {
        return description.isBlank()
                ? departmentType.getDefaultDescription()
                : description.trim();
    }

    private Department saveDepartment(Department department) {
        try {
            return departmentRepository.saveAndFlush(department);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.DEPARTMENT_TYPE_EXISTS);
        }
    }
}
