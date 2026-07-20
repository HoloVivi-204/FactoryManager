package com.factory.management.service.Service;

import com.factory.management.dto.request.ShiftRequest;
import com.factory.management.dto.request.ShiftUpdateRequest;
import com.factory.management.dto.response.ShiftResponse;

import java.util.List;

public interface ShiftService {
    ShiftResponse create(ShiftRequest request);

    List<ShiftResponse> getAll();

    ShiftResponse getById(Long id);

    ShiftResponse update(Long id, ShiftUpdateRequest request);

    void delete(Long id);
}
