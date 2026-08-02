package com.factory.management.repository;

import com.factory.management.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByCodeIgnoreCase(String code);

    Optional<Employee> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Employee> findAllByActiveTrue();

    Optional<Employee> findByIdAndActiveTrue(Long id);

    @Query("""
            select employee
            from Employee employee
                join employee.team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where team.id = :teamId
                and employee.active = true
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<Employee> findAllActiveByTeamIdInActiveHierarchy(@Param("teamId") Long teamId);

    @Query("""
            select employee
            from Employee employee
                join employee.team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where employee.id = :id
                and employee.active = true
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    Optional<Employee> findActiveByIdInActiveHierarchy(@Param("id") Long id);
}
