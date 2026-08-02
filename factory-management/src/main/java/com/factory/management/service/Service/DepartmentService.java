package com.factory.management.service.Service;

import com.factory.management.dto.request.DepartmentRequest;
import com.factory.management.dto.request.DepartmentUpdateRequest;
import com.factory.management.dto.response.DepartmentResponse;
import com.factory.management.dto.response.DepartmentTypeResponse;
import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);

    List<DepartmentResponse> getAll();

    List<DepartmentResponse> getAllByFactoryId(Long factoryId);

    List<DepartmentTypeResponse> getTypes();

    DepartmentResponse getById(Long id);

    DepartmentResponse update(Long id, DepartmentUpdateRequest request);

    void delete(Long id);
}
