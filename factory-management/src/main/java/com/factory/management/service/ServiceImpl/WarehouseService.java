package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.WarehouseRequest;
import com.factory.management.dto.response.WarehouseResponse;
import com.factory.management.entity.Factory;
import com.factory.management.entity.Warehouse;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.WarehouseRepository;
import com.factory.management.security.AuthorizationScope;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository repository;
    private final FactoryRepository factories;
    private final AuthorizationScope scope;

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (repository.existsByCodeIgnoreCase(code)) {
            throw new AppException(ErrorCode.WAREHOUSE_CODE_EXISTS);
        }

        Warehouse warehouse = new Warehouse();
        apply(warehouse, request, code);
        warehouse.setActive(request.getActive() == null || request.getActive());

        return response(repository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> all() {
        return repository.findAllByActiveTrueOrderByCode()
                .stream()
                .filter(this::canAccess)
                .map(this::response)
                .toList();
    }

    @Transactional
    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse warehouse = find(id);
        if (!canAccess(warehouse)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        String code = request.getCode().trim().toUpperCase();
        if (repository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new AppException(ErrorCode.WAREHOUSE_CODE_EXISTS);
        }

        apply(warehouse, request, code);
        if (request.getActive() != null) {
            warehouse.setActive(request.getActive());
        }

        return response(warehouse);
    }

    @Transactional
    public void delete(Long id) {
        Warehouse warehouse = find(id);
        if (!canAccess(warehouse)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        warehouse.setActive(false);
    }

    private void apply(Warehouse warehouse, WarehouseRequest request, String code) {
        if (!scope.canAccessFinancialScope(request.getFactoryId(), null, null)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        Factory factory = factories.findById(request.getFactoryId())
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND));
        warehouse.setCode(code);
        warehouse.setName(request.getName().trim());
        warehouse.setFactory(factory);
        warehouse.setDescription(request.getDescription());
    }

    private boolean canAccess(Warehouse warehouse) {
        return scope.canAccessFinancialScope(warehouse.getFactory().getId(), null, null);
    }

    private Warehouse find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
    }

    private WarehouseResponse response(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .factoryId(warehouse.getFactory().getId())
                .factoryName(warehouse.getFactory().getName())
                .description(warehouse.getDescription())
                .active(warehouse.getActive())
                .build();
    }
}
