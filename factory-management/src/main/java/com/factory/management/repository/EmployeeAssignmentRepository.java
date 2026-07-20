package com.factory.management.repository;

import com.factory.management.entity.EmployeeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;

public interface EmployeeAssignmentRepository
        extends JpaRepository<EmployeeAssignment, Long>, JpaSpecificationExecutor<EmployeeAssignment> {
    boolean existsByEmployee_IdAndActiveTrueAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            Long employeeId, LocalDate effectiveTo, LocalDate effectiveFrom);
}
