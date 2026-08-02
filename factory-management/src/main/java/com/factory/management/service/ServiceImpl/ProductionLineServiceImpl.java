package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.ProductionLineRequest;
import com.factory.management.dto.request.ProductionLineUpdateRequest;
import com.factory.management.dto.response.ProductionLineResponse;
import com.factory.management.entity.Department;
import com.factory.management.entity.ProductionLine;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.ProductionLineMapper;
import com.factory.management.repository.DepartmentRepository;
import com.factory.management.repository.ProductionLineRepository;
import com.factory.management.service.Service.ProductionLineService;
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
public class ProductionLineServiceImpl implements ProductionLineService {
    ProductionLineRepository productionLineRepository;
    DepartmentRepository departmentRepository;
    ProductionLineMapper productionLineMapper;

    @Override
    @Transactional
    public ProductionLineResponse create(ProductionLineRequest request) {
        Department department = findActiveDepartment(request.getDepartmentId());
        String normalizedCode = normalizeCode(request.getCode());

        if (productionLineRepository.existsByDepartment_IdAndCodeIgnoreCase(
                department.getId(), normalizedCode)) {
            throw new AppException(ErrorCode.PRODUCTION_LINE_CODE_EXISTS);
        }

        ProductionLine productionLine = productionLineMapper.mapToProductionLine(request);
        productionLine.setDepartment(department);
        applyNormalizedValues(productionLine, request, normalizedCode);

        if (productionLine.getActive() == null) {
            productionLine.setActive(true);
        }

        return productionLineMapper.mapToProductionLineResponse(saveProductionLine(productionLine));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionLineResponse> getAll() {
        return productionLineRepository
                .findAllActiveInActiveHierarchy()
                .stream()
                .map(productionLineMapper::mapToProductionLineResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionLineResponse> getAllByDepartmentId(Long departmentId) {
        findActiveDepartment(departmentId);
        return productionLineRepository
                .findAllActiveByDepartmentIdInActiveHierarchy(departmentId)
                .stream()
                .map(productionLineMapper::mapToProductionLineResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionLineResponse getById(Long id) {
        return productionLineMapper.mapToProductionLineResponse(findActiveProductionLine(id));
    }

    @Override
    @Transactional
    public ProductionLineResponse update(Long id, ProductionLineUpdateRequest request) {
        ProductionLine productionLine = findActiveProductionLine(id);
        Department department = request.getDepartmentId() == null
                ? productionLine.getDepartment()
                : findActiveDepartment(request.getDepartmentId());
        String normalizedCode = request.getCode() == null
                ? productionLine.getCode()
                : normalizeCode(request.getCode());

        if (productionLineRepository.existsByDepartment_IdAndCodeIgnoreCaseAndIdNot(
                department.getId(), normalizedCode, id)) {
            throw new AppException(ErrorCode.PRODUCTION_LINE_CODE_EXISTS);
        }

        productionLineMapper.updateProductionLineFromRequest(request, productionLine);
        productionLine.setDepartment(department);
        productionLine.setCode(normalizedCode);
        if (request.getName() != null) {
            productionLine.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            productionLine.setDescription(trimToNull(request.getDescription()));
        }

        return productionLineMapper.mapToProductionLineResponse(saveProductionLine(productionLine));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ProductionLine productionLine = findActiveProductionLine(id);
        productionLine.setActive(false);
    }

    private Department findActiveDepartment(Long id) {
        return departmentRepository.findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_ID_NOT_FOUND));
    }

    private ProductionLine findActiveProductionLine(Long id) {
        return productionLineRepository
                .findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_LINE_ID_NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private void applyNormalizedValues(
            ProductionLine productionLine,
            ProductionLineRequest request,
            String normalizedCode
    ) {
        productionLine.setCode(normalizedCode);
        productionLine.setName(request.getName().trim());
        productionLine.setDescription(trimToNull(request.getDescription()));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ProductionLine saveProductionLine(ProductionLine productionLine) {
        try {
            return productionLineRepository.saveAndFlush(productionLine);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.PRODUCTION_LINE_CODE_EXISTS);
        }
    }
}
