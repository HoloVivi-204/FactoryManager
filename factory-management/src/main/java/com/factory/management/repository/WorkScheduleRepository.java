package com.factory.management.repository;

import com.factory.management.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkScheduleRepository
        extends JpaRepository<WorkSchedule, Long>, JpaSpecificationExecutor<WorkSchedule> {
    List<WorkSchedule> findAllByEmployee_IdAndWorkDateBetweenAndActiveTrueOrderByWorkDate(
            Long id, LocalDate from, LocalDate to);

    Optional<WorkSchedule> findByEmployee_IdAndWorkDateAndActiveTrue(Long employeeId, LocalDate workDate);

    boolean existsByEmployee_IdAndWorkDateAndIdNot(Long employeeId, LocalDate workDate, Long id);
}
