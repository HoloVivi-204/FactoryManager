package com.factory.management.repository;

import com.factory.management.entity.ProductionPlan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductionPlanRepository
        extends JpaRepository<ProductionPlan, Long>, JpaSpecificationExecutor<ProductionPlan> {
    boolean existsByPlanNo(String planNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductionPlan p where p.id = :id")
    Optional<ProductionPlan> findByIdForUpdate(@Param("id") Long id);

    boolean existsByProduct_IdAndActiveTrue(Long productId);

    boolean existsByFactory_IdAndProductionLine_IdAndProduct_IdAndPeriodStartAndPeriodEndAndActiveTrue(
            Long factoryId,
            Long productionLineId,
            Long productId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    boolean existsByFactory_IdAndProductionLine_IdAndProduct_IdAndPeriodStartAndPeriodEndAndActiveTrueAndIdNot(
            Long factoryId,
            Long productionLineId,
            Long productId,
            LocalDate periodStart,
            LocalDate periodEnd,
            Long id
    );
}
