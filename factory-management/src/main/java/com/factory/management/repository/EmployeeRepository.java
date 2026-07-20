package com.factory.management.repository;

import com.factory.management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByCodeIgnoreCase(String code);

    Optional<Employee> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    List<Employee> findAllByActiveTrue();
    Optional<Employee> findByIdAndActiveTrue(Long id);

    List<Employee> findAllByActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue();

    List<Employee> findAllByTeam_IdAndActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(
            Long teamId
    );

    Optional<Employee> findByIdAndActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(
            Long id
    );

    Optional<Employee> findByIdAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(
            Long id
    );
}
