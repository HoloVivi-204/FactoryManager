package com.factory.management.service;

import com.factory.management.dto.request.FactoryRequest;
import com.factory.management.dto.request.FactoryUpdateRequest;
import com.factory.management.dto.response.FactoryResponse;
import java.util.List;

public interface FactoryService {
    FactoryResponse create(FactoryRequest request);

    List<FactoryResponse> getAll();

    FactoryResponse getById(Long id);

    FactoryResponse update(Long id, FactoryUpdateRequest request);

    void delete(Long id);
}
