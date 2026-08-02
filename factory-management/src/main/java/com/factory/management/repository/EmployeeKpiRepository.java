package com.factory.management.repository;

import com.factory.management.entity.EmployeeKpi;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeKpiRepository
        extends JpaRepository<EmployeeKpi, Long>, JpaSpecificationExecutor<EmployeeKpi> {
    List<EmployeeKpi> findAllByEmployee_IdOrderByPeriodEndDesc(Long id);

    boolean existsByEmployee_IdAndPeriodStartAndPeriodEndAndIdNot(
            Long employeeId, LocalDate periodStart, LocalDate periodEnd, Long id);
}
