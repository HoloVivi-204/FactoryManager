package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.EmployeeRequest;
import com.factory.management.modules.masterdata.dto.request.EmployeeUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.EmployeeResponse;
import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);

    List<EmployeeResponse> getAll();

    List<EmployeeResponse> getAllByTeamId(Long teamId);

    EmployeeResponse getById(Long id);

    EmployeeResponse update(Long id, EmployeeUpdateRequest request);

    void delete(Long id);
}
