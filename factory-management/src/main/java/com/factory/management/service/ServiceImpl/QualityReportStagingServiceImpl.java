package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.QualityReportStagingRequest;
import com.factory.management.dto.request.QualityReportStagingUpdateRequest;
import com.factory.management.dto.response.QualityReportStagingResponse;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.entity.QualityErrorType;
import com.factory.management.entity.QualityReportStaging;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.QualityReportStagingMapper;
import com.factory.management.repository.ProductionReportStagingRepository;
import com.factory.management.repository.QualityErrorTypeRepository;
import com.factory.management.repository.QualityReportStagingRepository;
import com.factory.management.service.Service.QualityReportStagingService;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityReportStagingServiceImpl implements QualityReportStagingService {
    QualityReportStagingRepository repository;
    ProductionReportStagingRepository reportRepository;
    QualityErrorTypeRepository errorTypeRepository;
    QualityReportStagingMapper mapper;

    @Override
    @Transactional
    public QualityReportStagingResponse create(QualityReportStagingRequest request) {
        ProductionReportStaging report = editableReport(request.getProductionReportStagingId());
        QualityErrorType type = activeErrorType(request.getQualityErrorTypeId());
        ensureUnique(report.getId(), type.getId(), null);

        boolean active = request.getActive() == null || request.getActive();
        if (active) {
            validateTotal(report, repository.sumActiveQuantityByReportId(report.getId()), request.getQuantity());
        }

        QualityReportStaging value = QualityReportStaging.builder()
                .productionReportStaging(report)
                .qualityErrorType(type)
                .quantity(request.getQuantity())
                .description(trimToNull(request.getDescription()))
                .active(active)
                .build();

        return mapper.mapToResponse(save(value));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityReportStagingResponse> getAll() {
        return map(repository.findAllByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityReportStagingResponse> getByReportId(Long id) {
        return map(repository.findAllByProductionReportStaging_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityReportStagingResponse> getByErrorTypeId(Long id) {
        return map(repository.findAllByQualityErrorType_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public QualityReportStagingResponse getById(Long id) {
        return mapper.mapToResponse(activeValue(id));
    }

    @Override
    @Transactional
    public QualityReportStagingResponse update(Long id, QualityReportStagingUpdateRequest request) {
        QualityReportStaging value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUALITY_REPORT_STAGING_NOT_FOUND));
        Long reportId = value.getProductionReportStaging().getId();
        requireSameReport(reportId, request.getProductionReportStagingId());

        ProductionReportStaging report = editableReport(reportId);
        Long typeId = request.getQualityErrorTypeId() == null
                ? value.getQualityErrorType().getId()
                : request.getQualityErrorTypeId();
        Long quantity = request.getQuantity() == null ? value.getQuantity() : request.getQuantity();
        boolean active = request.getActive() == null ? value.getActive() : request.getActive();
        QualityErrorType type = activeErrorType(typeId);
        ensureUnique(reportId, typeId, id);
        if (active) {
            Long otherTotal = repository.sumActiveQuantityByReportIdExcludingId(reportId, id);
            validateTotal(report, otherTotal, quantity);
        }

        value.setProductionReportStaging(report);
        value.setQualityErrorType(type);
        value.setQuantity(quantity);
        value.setActive(active);
        if (request.getDescription() != null) {
            value.setDescription(trimToNull(request.getDescription()));
        }

        return mapper.mapToResponse(save(value));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        QualityReportStaging value = activeValue(id);
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

    private QualityErrorType activeErrorType(Long id) {
        return errorTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUALITY_ERROR_TYPE_ID_NOT_FOUND));
    }

    private QualityReportStaging activeValue(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUALITY_REPORT_STAGING_NOT_FOUND));
    }

    private void validateTotal(ProductionReportStaging report, Long current, Long added) {
        if (current + added > report.getDefectQuantity()) {
            throw new AppException(ErrorCode.QUALITY_TOTAL_EXCEEDS_REPORT_DEFECT);
        }
    }

    private void ensureUnique(Long reportId, Long typeId, Long id) {
        boolean exists = id == null
                ? repository.existsByProductionReportStaging_IdAndQualityErrorType_Id(reportId, typeId)
                : repository.existsByProductionReportStaging_IdAndQualityErrorType_IdAndIdNot(reportId, typeId, id);
        if (exists) {
            throw new AppException(ErrorCode.QUALITY_REPORT_STAGING_EXISTS);
        }
    }

    private QualityReportStaging save(QualityReportStaging value) {
        try {
            return repository.saveAndFlush(value);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.QUALITY_REPORT_STAGING_EXISTS);
        }
    }

    private List<QualityReportStagingResponse> map(List<QualityReportStaging> values) {
        return values.stream()
                .map(mapper::mapToResponse)
                .toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
