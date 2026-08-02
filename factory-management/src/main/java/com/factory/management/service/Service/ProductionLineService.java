package com.factory.management.service.Service;

import com.factory.management.dto.request.ProductionLineRequest;
import com.factory.management.dto.request.ProductionLineUpdateRequest;
import com.factory.management.dto.response.ProductionLineResponse;
import java.util.List;

public interface ProductionLineService {
    ProductionLineResponse create(ProductionLineRequest request);

    List<ProductionLineResponse> getAll();

    List<ProductionLineResponse> getAllByDepartmentId(Long departmentId);

    ProductionLineResponse getById(Long id);

    ProductionLineResponse update(Long id, ProductionLineUpdateRequest request);

    void delete(Long id);
}
