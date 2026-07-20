package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.MaterialIssueStagingResponse;
import com.factory.management.entity.*;
import com.factory.management.exception.*;
import com.factory.management.mapper.MaterialIssueStagingMapper;
import com.factory.management.repository.*;
import com.factory.management.service.Service.MaterialIssueStagingService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaterialIssueStagingServiceImpl implements MaterialIssueStagingService {
    MaterialIssueStagingRepository repository;
    ProductionReportStagingRepository reportRepository;
    MaterialRepository materialRepository;
    MaterialIssueStagingMapper mapper;

    @Override @Transactional
    public MaterialIssueStagingResponse create(MaterialIssueStagingRequest r) {
        ProductionReportStaging report = editableReport(r.getProductionReportStagingId());
        Material material = activeMaterial(r.getMaterialId());
        MaterialIssueStaging value = MaterialIssueStaging.builder()
                .productionReportStaging(report).material(material).issueType(r.getIssueType())
                .quantity(r.getQuantity()).unit(r.getUnit().trim())
                .description(trimToNull(r.getDescription()))
                .active(r.getActive() == null ? true : r.getActive()).build();
        return mapper.mapToResponse(repository.save(value));
    }

    @Override @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getAll() { return map(repository.findAllByActiveTrue()); }
    @Override @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getByReportId(Long id) { return map(repository.findAllByProductionReportStaging_IdAndActiveTrue(id)); }
    @Override @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getByMaterialId(Long id) { return map(repository.findAllByMaterial_IdAndActiveTrue(id)); }
    @Override @Transactional(readOnly = true)
    public List<MaterialIssueStagingResponse> getByIssueType(MaterialIssueType type) { return map(repository.findAllByIssueTypeAndActiveTrue(type)); }
    @Override @Transactional(readOnly = true)
    public MaterialIssueStagingResponse getById(Long id) { return mapper.mapToResponse(activeValue(id)); }

    @Override @Transactional
    public MaterialIssueStagingResponse update(Long id, MaterialIssueStagingUpdateRequest r) {
        MaterialIssueStaging value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ISSUE_STAGING_NOT_FOUND));
        Long reportId = value.getProductionReportStaging().getId();
        requireSameReport(reportId, r.getProductionReportStagingId());
        ProductionReportStaging report = editableReport(reportId);
        Long materialId = r.getMaterialId() == null ? value.getMaterial().getId() : r.getMaterialId();
        Material material = activeMaterial(materialId);
        value.setProductionReportStaging(report); value.setMaterial(material);
        if (r.getIssueType() != null) value.setIssueType(r.getIssueType());
        if (r.getQuantity() != null) value.setQuantity(r.getQuantity());
        if (r.getUnit() != null) value.setUnit(r.getUnit().trim());
        if (r.getDescription() != null) value.setDescription(trimToNull(r.getDescription()));
        if (r.getActive() != null) value.setActive(r.getActive());
        return mapper.mapToResponse(repository.save(value));
    }

    @Override @Transactional
    public void delete(Long id) {
        MaterialIssueStaging value = activeValue(id);
        editableReport(value.getProductionReportStaging().getId());
        value.setActive(false);
    }

    private ProductionReportStaging editableReport(Long id) {
        ProductionReportStaging report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_STAGING_NOT_FOUND));
        if (report.getStatus() != ProductionReportStatus.DRAFT)
            throw new AppException(ErrorCode.STAGING_DETAIL_REQUIRES_DRAFT);
        return report;
    }
    private void requireSameReport(Long currentId, Long requestedId) { if (requestedId != null && !requestedId.equals(currentId)) throw new AppException(ErrorCode.STAGING_DETAIL_PARENT_IMMUTABLE); }
    private Material activeMaterial(Long id) { return materialRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ID_NOT_FOUND)); }
    private MaterialIssueStaging activeValue(Long id) { return repository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ISSUE_STAGING_NOT_FOUND)); }
    private List<MaterialIssueStagingResponse> map(List<MaterialIssueStaging> list) { return list.stream().map(mapper::mapToResponse).toList(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
