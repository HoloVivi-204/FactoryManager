package com.factory.management.service.impl;

import com.factory.management.dto.request.MaterialIssueStagingRequest;
import com.factory.management.dto.request.MaterialIssueStagingUpdateRequest;
import com.factory.management.dto.response.MaterialIssueStagingResponse;
import com.factory.management.entity.Material;
import com.factory.management.entity.MaterialIssueStaging;
import com.factory.management.entity.MaterialIssueType;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.MaterialIssueStagingMapper;
import com.factory.management.repository.MaterialIssueStagingRepository;
import com.factory.management.repository.MaterialRepository;
import com.factory.management.repository.ProductionReportStagingRepository;
import com.factory.management.service.MaterialIssueStagingService;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaterialIssueStagingServiceImpl implements MaterialIssueStagingService {
    MaterialIssueStagingRepository repository;
    ProductionReportStagingRepository reportRepository;
    MaterialRepository materialRepository;
    MaterialIssueStagingMapper mapper;

    @Override
    @Transactional
    public MaterialIssueStagingResponse create(MaterialIssueStagingRequest request) {
        ProductionReportStaging report = editableReport(request.getProductionReportStagingId());
        Material material = activeMaterial(request.getMaterialId());

        MaterialIssueStaging value = MaterialIssueStaging.builder()
                .productionReportStaging(report)
                .material(material)
                .issueType(request.getIssueType())
                .quantity(request.getQuantity())
                .unit(request.getUnit().trim())
                .description(trimToNull(request.getDescription()))
                .active(request.getActive() == null ? true : request.getActive())
                .build();

        return mapper.mapToResponse(repository.save(value));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getAll() {
        return map(repository.findAllByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getByReportId(Long id) {
        return map(repository.findAllByProductionReportStaging_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getByMaterialId(Long id) {
        return map(repository.findAllByMaterial_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getByIssueType(MaterialIssueType type) {
        return map(repository.findAllByIssueTypeAndActiveTrue(type));
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialIssueStagingResponse getById(Long id) {
        return mapper.mapToResponse(activeValue(id));
    }

    @Override
    @Transactional
    public MaterialIssueStagingResponse update(Long id, MaterialIssueStagingUpdateRequest request) {
        MaterialIssueStaging value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ISSUE_STAGING_NOT_FOUND));
        Long reportId = value.getProductionReportStaging().getId();
        requireSameReport(reportId, request.getProductionReportStagingId());

        ProductionReportStaging report = editableReport(reportId);
        Long materialId = request.getMaterialId() == null ? value.getMaterial().getId() : request.getMaterialId();
        Material material = activeMaterial(materialId);

        value.setProductionReportStaging(report);
        value.setMaterial(material);
        if (request.getIssueType() != null) {
            value.setIssueType(request.getIssueType());
        }
        if (request.getQuantity() != null) {
            value.setQuantity(request.getQuantity());
        }
        if (request.getUnit() != null) {
            value.setUnit(request.getUnit().trim());
        }
        if (request.getDescription() != null) {
            value.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getActive() != null) {
            value.setActive(request.getActive());
        }

        return mapper.mapToResponse(repository.save(value));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MaterialIssueStaging value = activeValue(id);
        editableReport(value.getProductionReportStaging().getId());
        value.setActive(false);
    }

    private ProductionReportStaging editableReport(Long id) {
        ProductionReportStaging report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_STAGING_NOT_FOUND));
        if (report.getStatus() != ProductionReportStatus.DRAFT) {
            throw new AppException(ErrorCode.STAGING_DETAIL_REQUIRES_DRAFT);
        }

        return report;
    }

    private void requireSameReport(Long currentId, Long requestedId) {
        if (requestedId != null && !requestedId.equals(currentId)) {
            throw new AppException(ErrorCode.STAGING_DETAIL_PARENT_IMMUTABLE);
        }
    }

    private Material activeMaterial(Long id) {
        return materialRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ID_NOT_FOUND));
    }

    private MaterialIssueStaging activeValue(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ISSUE_STAGING_NOT_FOUND));
    }

    private List<MaterialIssueStagingResponse> map(List<MaterialIssueStaging> values) {
        return values.stream()
                .map(mapper::mapToResponse)
                .toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
