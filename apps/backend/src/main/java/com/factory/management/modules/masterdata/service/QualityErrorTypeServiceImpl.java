package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.QualityErrorTypeRequest;
import com.factory.management.modules.masterdata.dto.request.QualityErrorTypeUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.QualityErrorTypeResponse;
import com.factory.management.modules.masterdata.entity.QualityErrorSeverity;
import com.factory.management.modules.masterdata.entity.QualityErrorType;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.modules.masterdata.mapper.QualityErrorTypeMapper;
import com.factory.management.modules.masterdata.repository.QualityErrorTypeRepository;
import com.factory.management.modules.masterdata.service.QualityErrorTypeService;
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
public class QualityErrorTypeServiceImpl implements QualityErrorTypeService {
    QualityErrorTypeRepository qualityErrorTypeRepository;
    QualityErrorTypeMapper qualityErrorTypeMapper;

    @Override
    @Transactional
    public QualityErrorTypeResponse create(QualityErrorTypeRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (qualityErrorTypeRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.QUALITY_ERROR_TYPE_CODE_EXISTS);
        }

        QualityErrorType qualityErrorType = qualityErrorTypeMapper.mapToQualityErrorType(request);
        qualityErrorType.setCode(normalizedCode);
        qualityErrorType.setName(request.getName().trim());
        qualityErrorType.setDescription(trimToNull(request.getDescription()));

        if (qualityErrorType.getActive() == null) {
            qualityErrorType.setActive(true);
        }

        return qualityErrorTypeMapper.mapToQualityErrorTypeResponse(saveQualityErrorType(qualityErrorType));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityErrorTypeResponse> getAll() {
        return qualityErrorTypeRepository.findAllByActiveTrue().stream()
                .map(qualityErrorTypeMapper::mapToQualityErrorTypeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QualityErrorTypeResponse> getAllBySeverity(QualityErrorSeverity severity) {
        return qualityErrorTypeRepository.findAllBySeverityAndActiveTrue(severity).stream()
                .map(qualityErrorTypeMapper::mapToQualityErrorTypeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QualityErrorTypeResponse getById(Long id) {
        return qualityErrorTypeMapper.mapToQualityErrorTypeResponse(findActiveQualityErrorType(id));
    }

    @Override
    @Transactional
    public QualityErrorTypeResponse update(Long id, QualityErrorTypeUpdateRequest request) {
        QualityErrorType qualityErrorType = findQualityErrorTypeForUpdate(id);
        String normalizedCode = request.getCode() == null
                ? qualityErrorType.getCode()
                : normalizeCode(request.getCode());

        if (qualityErrorTypeRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new AppException(ErrorCode.QUALITY_ERROR_TYPE_CODE_EXISTS);
        }

        qualityErrorTypeMapper.updateQualityErrorTypeFromRequest(request, qualityErrorType);
        qualityErrorType.setCode(normalizedCode);

        if (request.getName() != null) {
            qualityErrorType.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            qualityErrorType.setDescription(trimToNull(request.getDescription()));
        }

        return qualityErrorTypeMapper.mapToQualityErrorTypeResponse(saveQualityErrorType(qualityErrorType));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        QualityErrorType qualityErrorType = findActiveQualityErrorType(id);
        qualityErrorType.setActive(false);
    }

    private QualityErrorType findActiveQualityErrorType(Long id) {
        return qualityErrorTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUALITY_ERROR_TYPE_ID_NOT_FOUND));
    }

    private QualityErrorType findQualityErrorTypeForUpdate(Long id) {
        return qualityErrorTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUALITY_ERROR_TYPE_ID_NOT_FOUND));
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

    private QualityErrorType saveQualityErrorType(QualityErrorType qualityErrorType) {
        try {
            return qualityErrorTypeRepository.saveAndFlush(qualityErrorType);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.QUALITY_ERROR_TYPE_CODE_EXISTS);
        }
    }
}
