package com.factory.management.modules.production.service;

import com.factory.management.modules.production.dto.request.EmployeeActualStagingRequest;
import com.factory.management.modules.production.dto.request.EmployeeActualStagingUpdateRequest;
import com.factory.management.modules.production.dto.response.EmployeeActualStagingResponse;
import java.util.List;

public interface EmployeeActualStagingService {
    EmployeeActualStagingResponse create(EmployeeActualStagingRequest request);
    List<EmployeeActualStagingResponse> getAll();
    List<EmployeeActualStagingResponse> getByReportId(Long reportId);
    List<EmployeeActualStagingResponse> getByEmployeeId(Long employeeId);
    EmployeeActualStagingResponse getById(Long id);
    EmployeeActualStagingResponse update(Long id, EmployeeActualStagingUpdateRequest request);
    void delete(Long id);
}
