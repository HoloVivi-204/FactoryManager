package com.factory.management.repository;

import com.factory.management.entity.FinancialRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FinancialRecordRepository
        extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {
    boolean existsByRecordNo(String recordNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select value from FinancialRecord value where value.id = :id")
    Optional<FinancialRecord> findForUpdate(@Param("id") Long id);
}
