package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.MachineRequest;
import com.factory.management.modules.masterdata.dto.request.MachineUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.MachineResponse;
import com.factory.management.modules.maintenance.entity.MachineOperationalStatus;
import java.util.List;

public interface MachineService {
    MachineResponse create(MachineRequest request);

    List<MachineResponse> getAll();

    List<MachineResponse> getAllByTeamId(Long teamId);

    List<MachineResponse> getAllByMachineTypeId(Long machineTypeId);

    List<MachineResponse> getAllByOperationalStatus(MachineOperationalStatus operationalStatus);

    MachineResponse getById(Long id);

    MachineResponse update(Long id, MachineUpdateRequest request);

    void delete(Long id);
}
