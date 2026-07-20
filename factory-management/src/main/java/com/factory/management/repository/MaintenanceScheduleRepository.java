package com.factory.management.repository;

import com.factory.management.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MaintenanceScheduleRepository
        extends JpaRepository<MaintenanceSchedule, Long>, JpaSpecificationExecutor<MaintenanceSchedule> {
}
