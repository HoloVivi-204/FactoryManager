package com.factory.management.service.Service;

import com.factory.management.dto.request.QualityErrorTypeRequest;
import com.factory.management.dto.request.QualityErrorTypeUpdateRequest;
import com.factory.management.dto.response.QualityErrorTypeResponse;
import com.factory.management.entity.QualityErrorSeverity;
import java.util.List;

public interface QualityErrorTypeService {
    QualityErrorTypeResponse create(QualityErrorTypeRequest request);

    List<QualityErrorTypeResponse> getAll();

    List<QualityErrorTypeResponse> getAllBySeverity(QualityErrorSeverity severity);

    QualityErrorTypeResponse getById(Long id);

    QualityErrorTypeResponse update(Long id, QualityErrorTypeUpdateRequest request);

    void delete(Long id);
}
