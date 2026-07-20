package com.factory.management.repository;

import com.factory.management.entity.MaintenancePartUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface MaintenancePartUsageRepository extends JpaRepository<MaintenancePartUsage, Long> {
    List<MaintenancePartUsage> findAllByWorkOrder_IdAndActiveTrue(Long workOrderId);
    List<MaintenancePartUsage> findAllByWorkOrder_IdInAndActiveTrue(Collection<Long> workOrderIds);
    boolean existsByWorkOrder_IdAndMaterial_IdAndActiveTrue(Long workOrderId, Long materialId);
}
