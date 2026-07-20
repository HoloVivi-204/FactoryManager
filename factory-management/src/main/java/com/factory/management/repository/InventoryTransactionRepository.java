package com.factory.management.repository;

import com.factory.management.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction> {
    boolean existsByTransactionNo(String no);

    List<InventoryTransaction> findAllByWarehouse_IdAndMaterial_IdAndActiveTrue(Long warehouseId, Long materialId);
}
