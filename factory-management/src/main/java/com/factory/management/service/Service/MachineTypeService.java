package com.factory.management.service.Service;

import com.factory.management.dto.request.MachineTypeRequest;
import com.factory.management.dto.request.MachineTypeUpdateRequest;
import com.factory.management.dto.response.MachineTypeResponse;
import java.util.List;

public interface MachineTypeService {
    MachineTypeResponse create(MachineTypeRequest request);

    List<MachineTypeResponse> getAll();

    MachineTypeResponse getById(Long id);

    MachineTypeResponse update(Long id, MachineTypeUpdateRequest request);

    void delete(Long id);
}
