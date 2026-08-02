package com.factory.management.modules.production.service;

import com.factory.management.modules.production.dto.request.MachineDowntimeStagingRequest;
import com.factory.management.modules.production.dto.request.MachineDowntimeStagingUpdateRequest;
import com.factory.management.modules.production.dto.response.MachineDowntimeStagingResponse;
import java.util.List;

public interface MachineDowntimeStagingService {
    MachineDowntimeStagingResponse create(MachineDowntimeStagingRequest request);
    List<MachineDowntimeStagingResponse> getAll();
    List<MachineDowntimeStagingResponse> getByReportId(Long reportId);
    List<MachineDowntimeStagingResponse> getByMachineId(Long machineId);
    MachineDowntimeStagingResponse getById(Long id);
    MachineDowntimeStagingResponse update(Long id, MachineDowntimeStagingUpdateRequest request);
    void delete(Long id);
}
