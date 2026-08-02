package com.factory.management.service.Service;

import com.factory.management.dto.request.DowntimeReasonRequest;
import com.factory.management.dto.request.DowntimeReasonUpdateRequest;
import com.factory.management.dto.response.DowntimeReasonResponse;
import com.factory.management.entity.DowntimeReasonType;
import java.util.List;

public interface DowntimeReasonService {
    DowntimeReasonResponse create(DowntimeReasonRequest request);

    List<DowntimeReasonResponse> getAll();

    List<DowntimeReasonResponse> getAllByReasonType(DowntimeReasonType reasonType);

    DowntimeReasonResponse getById(Long id);

    DowntimeReasonResponse update(Long id, DowntimeReasonUpdateRequest request);

    void delete(Long id);
}
