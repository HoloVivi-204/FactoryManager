package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.FactoryRequest;
import com.factory.management.modules.masterdata.dto.request.FactoryUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.FactoryResponse;
import java.util.List;

public interface FactoryService {
    FactoryResponse create(FactoryRequest request);

    List<FactoryResponse> getAll();

    FactoryResponse getById(Long id);

    FactoryResponse update(Long id, FactoryUpdateRequest request);

    void delete(Long id);
}
