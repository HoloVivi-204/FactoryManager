package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.ProductionReport;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductionReportRepository extends JpaRepository<ProductionReport, Long>,
        JpaSpecificationExecutor<ProductionReport> {
    boolean existsBySourceStaging_Id(Long stagingId);
    long countByReportDate(LocalDate reportDate);
    List<ProductionReport> findAllByReportDate(LocalDate reportDate);
    List<ProductionReport> findAllByTeam_Id(Long teamId);
    List<ProductionReport> findAllByMachine_Id(Long machineId);
    Optional<ProductionReport> findByReportNo(String reportNo);
}
