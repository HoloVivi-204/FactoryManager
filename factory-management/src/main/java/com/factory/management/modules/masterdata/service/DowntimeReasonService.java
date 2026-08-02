package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.DowntimeReasonRequest;
import com.factory.management.modules.masterdata.dto.request.DowntimeReasonUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.DowntimeReasonResponse;
import com.factory.management.modules.masterdata.entity.DowntimeReasonType;
import java.util.List;

public interface DowntimeReasonService {
    DowntimeReasonResponse create(DowntimeReasonRequest request);

    List<DowntimeReasonResponse> getAll();

    List<DowntimeReasonResponse> getAllByReasonType(DowntimeReasonType reasonType);

    DowntimeReasonResponse getById(Long id);

    DowntimeReasonResponse update(Long id, DowntimeReasonUpdateRequest request);

    void delete(Long id);
}
