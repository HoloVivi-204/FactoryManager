package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.EmployeeActual;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeActualRepository extends JpaRepository<EmployeeActual, Long> {
    List<EmployeeActual> findAllByProductionReport_Id(Long id);

    List<EmployeeActual> findAllByProductionReport_IdIn(Collection<Long> ids);

    List<EmployeeActual> findAllByEmployee_Id(Long id);
}
