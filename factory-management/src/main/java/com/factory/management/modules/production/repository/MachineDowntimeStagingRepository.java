package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.MachineDowntimeStaging;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

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
