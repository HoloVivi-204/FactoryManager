package com.factory.management.modules.maintenance.repository;

import com.factory.management.modules.maintenance.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaintenanceScheduleRepository
        extends JpaRepository<MaintenanceSchedule, Long>, JpaSpecificationExecutor<MaintenanceSchedule> {
}
