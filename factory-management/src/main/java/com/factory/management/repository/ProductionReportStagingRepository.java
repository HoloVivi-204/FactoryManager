package com.factory.management.repository;

import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionReportStagingRepository extends JpaRepository<ProductionReportStaging, Long> {
    boolean existsByReportDateAndShift_IdAndTeam_IdAndMachine_Id(
            LocalDate reportDate, Long shiftId, Long teamId, Long machineId);

    boolean existsByReportDateAndShift_IdAndTeam_IdAndMachine_IdAndIdNot(
            LocalDate reportDate, Long shiftId, Long teamId, Long machineId, Long id);

    Optional<ProductionReportStaging> findByReportDateAndShift_IdAndTeam_IdAndMachine_Id(
            LocalDate reportDate, Long shiftId, Long teamId, Long machineId);

    List<ProductionReportStaging> findAllByReportDate(LocalDate reportDate);
    List<ProductionReportStaging> findAllByTeam_Id(Long teamId);
    List<ProductionReportStaging> findAllByTeam_IdInOrderByReportDateDescIdDesc(Collection<Long> teamIds);
    List<ProductionReportStaging> findAllByTeam_IdInAndReportDateBetweenOrderByReportDateDescIdDesc(
            Collection<Long> teamIds,
            LocalDate fromDate,
            LocalDate toDate
    );
    List<ProductionReportStaging> findAllByMachine_Id(Long machineId);
    List<ProductionReportStaging> findAllByStatus(ProductionReportStatus status);
}
