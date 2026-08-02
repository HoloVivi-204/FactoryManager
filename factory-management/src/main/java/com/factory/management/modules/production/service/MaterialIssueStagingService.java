package com.factory.management.modules.production.service;

import com.factory.management.modules.production.dto.request.MaterialIssueStagingRequest;
import com.factory.management.modules.production.dto.request.MaterialIssueStagingUpdateRequest;
import com.factory.management.modules.production.dto.response.MaterialIssueStagingResponse;
import com.factory.management.modules.production.entity.MaterialIssueType;
import java.util.List;

public interface MaterialIssueStagingService {
    MaterialIssueStagingResponse create(MaterialIssueStagingRequest request);
    List<MaterialIssueStagingResponse> getAll();
    List<MaterialIssueStagingResponse> getByReportId(Long reportId);
    List<MaterialIssueStagingResponse> getByMaterialId(Long materialId);
    List<MaterialIssueStagingResponse> getByIssueType(MaterialIssueType issueType);
    MaterialIssueStagingResponse getById(Long id);
    MaterialIssueStagingResponse update(Long id, MaterialIssueStagingUpdateRequest request);
    void delete(Long id);
}
