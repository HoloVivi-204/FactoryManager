package com.factory.management.repository;

import com.factory.management.entity.MaintenancePartUsage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenancePartUsageRepository extends JpaRepository<MaintenancePartUsage, Long> {
    List<MaintenancePartUsage> findAllByWorkOrder_IdAndActiveTrue(Long workOrderId);
    List<MaintenancePartUsage> findAllByWorkOrder_IdInAndActiveTrue(Collection<Long> workOrderIds);
    boolean existsByWorkOrder_IdAndMaterial_IdAndActiveTrue(Long workOrderId, Long materialId);
}
