package com.factory.management.modules.masterdata.service;

import com.factory.management.modules.masterdata.dto.request.MachineTypeRequest;
import com.factory.management.modules.masterdata.dto.request.MachineTypeUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.MachineTypeResponse;
import java.util.List;

public interface MachineTypeService {
    MachineTypeResponse create(MachineTypeRequest request);

    List<MachineTypeResponse> getAll();

    MachineTypeResponse getById(Long id);

    MachineTypeResponse update(Long id, MachineTypeUpdateRequest request);

    void delete(Long id);
}
