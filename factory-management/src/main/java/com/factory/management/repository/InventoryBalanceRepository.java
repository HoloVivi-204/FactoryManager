package com.factory.management.repository;

import com.factory.management.entity.InventoryBalance;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select value from InventoryBalance value where value.warehouse.id = :warehouseId and value.material.id = :materialId")
    Optional<InventoryBalance> findForUpdate(
            @Param("warehouseId") Long warehouseId,
            @Param("materialId") Long materialId
    );

    List<InventoryBalance> findAllByWarehouse_Id(Long warehouseId);
}
