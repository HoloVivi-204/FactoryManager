package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.QualityErrorTypeRequest;
import com.factory.management.modules.masterdata.dto.request.QualityErrorTypeUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.QualityErrorTypeResponse;
import com.factory.management.modules.masterdata.entity.QualityErrorSeverity;
import java.util.List;

public interface QualityErrorTypeService {
    QualityErrorTypeResponse create(QualityErrorTypeRequest request);

    List<QualityErrorTypeResponse> getAll();

    List<QualityErrorTypeResponse> getAllBySeverity(QualityErrorSeverity severity);

    QualityErrorTypeResponse getById(Long id);

    QualityErrorTypeResponse update(Long id, QualityErrorTypeUpdateRequest request);

    void delete(Long id);
}
