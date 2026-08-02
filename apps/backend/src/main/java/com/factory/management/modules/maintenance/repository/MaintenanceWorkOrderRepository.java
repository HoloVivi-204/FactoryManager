package com.factory.management.modules.maintenance.repository;

import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrder;
import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaintenanceWorkOrderRepository
        extends JpaRepository<MaintenanceWorkOrder, Long>, JpaSpecificationExecutor<MaintenanceWorkOrder> {
    boolean existsByWorkOrderNo(String workOrderNo);

    boolean existsByMachine_IdAndStatusAndIdNot(
            Long machineId,
            MaintenanceWorkOrderStatus status,
            Long id
    );
}
