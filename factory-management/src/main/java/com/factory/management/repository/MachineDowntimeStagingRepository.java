package com.factory.management.repository;

import com.factory.management.entity.MachineDowntimeStaging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.time.LocalDateTime;

public interface MachineDowntimeStagingRepository extends JpaRepository<MachineDowntimeStaging, Long> {
    List<MachineDowntimeStaging> findAllByActiveTrue();
    List<MachineDowntimeStaging> findAllByProductionReportStaging_IdAndActiveTrue(Long reportId);
    List<MachineDowntimeStaging> findAllByProductionReportStaging_IdInAndActiveTrue(Collection<Long> reportIds);
    List<MachineDowntimeStaging> findAllByMachine_IdAndActiveTrue(Long machineId);
    Optional<MachineDowntimeStaging> findByIdAndActiveTrue(Long id);
    Optional<MachineDowntimeStaging> findByProductionReportStaging_IdAndMachine_IdAndDowntimeReason_IdAndStartTimeAndEndTime(
            Long reportId, Long machineId, Long reasonId, LocalDateTime startTime, LocalDateTime endTime);
    long deleteAllByProductionReportStaging_Id(Long reportId);
}
