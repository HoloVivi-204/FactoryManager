package com.factory.management.repository;

import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ProductionReportCloseRepository extends Repository<ProductionReportStaging, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductionReportStaging p " +
            "where p.reportDate = :reportDate and p.status in :statuses order by p.id")
    List<ProductionReportStaging> findApprovedForClose(
            @Param("reportDate") LocalDate reportDate,
            @Param("statuses") Collection<ProductionReportStatus> statuses
    );
}
