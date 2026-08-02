package com.factory.management.service.Service;

import com.factory.management.dto.request.QualityReportStagingRequest;
import com.factory.management.dto.request.QualityReportStagingUpdateRequest;
import com.factory.management.dto.response.QualityReportStagingResponse;
import java.util.List;

public interface QualityReportStagingService {
    QualityReportStagingResponse create(QualityReportStagingRequest request);
    List<QualityReportStagingResponse> getAll();
    List<QualityReportStagingResponse> getByReportId(Long reportId);
    List<QualityReportStagingResponse> getByErrorTypeId(Long errorTypeId);
    QualityReportStagingResponse getById(Long id);
    QualityReportStagingResponse update(Long id, QualityReportStagingUpdateRequest request);
    void delete(Long id);
}
