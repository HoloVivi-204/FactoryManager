package com.factory.management.modules.inventory.repository;

import com.factory.management.modules.inventory.entity.InventoryTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction> {
    boolean existsByTransactionNo(String no);

    List<InventoryTransaction> findAllByWarehouse_IdAndMaterial_IdAndActiveTrue(Long warehouseId, Long materialId);
}
