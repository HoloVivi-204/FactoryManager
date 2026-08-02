package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.MaterialRequest;
import com.factory.management.modules.masterdata.dto.request.MaterialUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.MaterialResponse;
import java.util.List;

public interface MaterialService {
    MaterialResponse create(MaterialRequest request);

    List<MaterialResponse> getAll();

    MaterialResponse getById(Long id);

    MaterialResponse update(Long id, MaterialUpdateRequest request);

    void delete(Long id);
}
