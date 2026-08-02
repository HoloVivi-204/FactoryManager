package com.factory.management.modules.hr.repository;

import com.factory.management.modules.hr.entity.LeaveRequest;
import com.factory.management.modules.hr.entity.LeaveStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {
    List<LeaveRequest> findAllByEmployee_IdOrderByCreatedAtDesc(Long id);

    boolean existsByEmployee_IdAndStatusAndActiveTrueAndFromDateLessThanEqualAndToDateGreaterThanEqual(
            Long employeeId, LeaveStatus status, LocalDate toDate, LocalDate fromDate);
}
