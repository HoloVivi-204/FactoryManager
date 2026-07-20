package com.factory.management.service.Service;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.EmployeeActualStagingResponse;

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
