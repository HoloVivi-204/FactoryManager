package com.factory.management.repository;

import com.factory.management.entity.ProductionOrder;
import com.factory.management.entity.ProductionOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;

public interface ProductionOrderRepository
        extends JpaRepository<ProductionOrder, Long>, JpaSpecificationExecutor<ProductionOrder> {
    boolean existsByOrderNo(String orderNo);

    boolean existsByProductionPlan_IdAndActiveTrue(Long planId);

    boolean existsByProductionPlan_IdAndActiveTrueAndStatusNotIn(
            Long planId,
            Collection<ProductionOrderStatus> statuses
    );

    @Query("""
            select coalesce(sum(o.plannedQuantity), 0)
            from ProductionOrder o
            where o.productionPlan.id = :planId
              and o.active = true
              and o.status <> com.factory.management.entity.ProductionOrderStatus.CANCELLED
              and (:excludeId is null or o.id <> :excludeId)
            """)
    BigDecimal sumActivePlannedQuantity(
            @Param("planId") Long planId,
            @Param("excludeId") Long excludeId
    );
}
