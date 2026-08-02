package com.factory.management.repository;

import com.factory.management.entity.OvertimeRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OvertimeRequestRepository
        extends JpaRepository<OvertimeRequest, Long>, JpaSpecificationExecutor<OvertimeRequest> {
    List<OvertimeRequest> findAllByEmployee_IdOrderByCreatedAtDesc(Long employeeId);
    boolean existsByEmployee_IdAndWorkDateAndIdNot(Long employeeId, LocalDate workDate, Long id);
}
