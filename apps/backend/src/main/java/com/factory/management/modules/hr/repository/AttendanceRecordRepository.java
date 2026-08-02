package com.factory.management.modules.hr.repository;

import com.factory.management.modules.hr.entity.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttendanceRecordRepository
        extends JpaRepository<AttendanceRecord, Long>, JpaSpecificationExecutor<AttendanceRecord> {
    List<AttendanceRecord> findAllByEmployee_IdAndWorkDateBetweenOrderByWorkDateDesc(
            Long id, LocalDate from, LocalDate to);

    Optional<AttendanceRecord> findByEmployee_IdAndWorkDate(Long employeeId, LocalDate workDate);
}
