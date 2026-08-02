package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.ShiftRequest;
import com.factory.management.modules.masterdata.dto.request.ShiftUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.ShiftResponse;
import java.util.List;

public interface ShiftService {
    ShiftResponse create(ShiftRequest request);

    List<ShiftResponse> getAll();

    ShiftResponse getById(Long id);

    ShiftResponse update(Long id, ShiftUpdateRequest request);

    void delete(Long id);
}
