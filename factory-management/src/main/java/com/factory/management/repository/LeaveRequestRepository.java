package com.factory.management.repository;

import com.factory.management.entity.LeaveRequest;
import com.factory.management.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    List<LeaveRequest> findAllByEmployee_IdOrderByCreatedAtDesc(Long id);

    boolean existsByEmployee_IdAndStatusAndActiveTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId, LeaveStatus status, LocalDate toDate, LocalDate fromDate);
}
