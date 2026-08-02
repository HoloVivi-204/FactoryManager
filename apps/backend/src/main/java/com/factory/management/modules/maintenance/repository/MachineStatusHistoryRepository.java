package com.factory.management.modules.maintenance.repository;

import com.factory.management.modules.maintenance.entity.MachineStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineStatusHistoryRepository extends JpaRepository<MachineStatusHistory, Long> {
    Page<MachineStatusHistory> findAllByMachine_Id(Long machineId, Pageable pageable);
}
