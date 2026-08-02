package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.EmployeeActualStaging;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeActualStagingRepository extends JpaRepository<EmployeeActualStaging, Long> {
    boolean existsByProductionReportStaging_IdAndEmployee_Id(Long reportId, Long employeeId);
    boolean existsByProductionReportStaging_IdAndEmployee_IdAndIdNot(Long reportId, Long employeeId, Long id);
    List<EmployeeActualStaging> findAllByActiveTrue();
    List<EmployeeActualStaging> findAllByProductionReportStaging_IdAndActiveTrue(Long reportId);
    List<EmployeeActualStaging> findAllByProductionReportStaging_IdInAndActiveTrue(Collection<Long> reportIds);
    List<EmployeeActualStaging> findAllByEmployee_IdAndActiveTrue(Long employeeId);
    Optional<EmployeeActualStaging> findByIdAndActiveTrue(Long id);
    Optional<EmployeeActualStaging> findByProductionReportStaging_IdAndEmployee_Id(Long reportId, Long employeeId);
    long deleteAllByProductionReportStaging_Id(Long reportId);
}
