package com.factory.management.service.Service;

import com.factory.management.dto.request.EmployeeRequest;
import com.factory.management.dto.request.EmployeeUpdateRequest;
import com.factory.management.dto.response.EmployeeResponse;
import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);

    List<EmployeeResponse> getAll();

    List<EmployeeResponse> getAllByTeamId(Long teamId);

    EmployeeResponse getById(Long id);

    EmployeeResponse update(Long id, EmployeeUpdateRequest request);

    void delete(Long id);
}
