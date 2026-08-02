package com.factory.management.repository;

import com.factory.management.entity.Department;
import com.factory.management.entity.DepartmentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByFactory_IdAndCodeIgnoreCase(Long factoryId, String code);

    boolean existsByFactory_IdAndCodeIgnoreCaseAndIdNot(Long factoryId, String code, Long id);

    boolean existsByFactory_IdAndDepartmentType(Long factoryId, DepartmentType departmentType);

    boolean existsByFactory_IdAndDepartmentTypeAndIdNot(
            Long factoryId,
            DepartmentType departmentType,
            Long id
    );

    @Query("""
            select department
            from Department department
                join department.factory factory
            where department.active = true
                and factory.active = true
            """)
    List<Department> findAllActiveInActiveHierarchy();

    @Query("""
            select department
            from Department department
                join department.factory factory
            where factory.id = :factoryId
                and department.active = true
                and factory.active = true
            """)
    List<Department> findAllActiveByFactoryIdInActiveHierarchy(@Param("factoryId") Long factoryId);

    @Query("""
            select department
            from Department department
                join department.factory factory
            where department.id = :id
                and department.active = true
                and factory.active = true
            """)
    Optional<Department> findActiveByIdInActiveHierarchy(@Param("id") Long id);

    @Query("""
            select department
            from Department department
                join department.factory factory
            where department.id = :id
                and factory.active = true
            """)
    Optional<Department> findByIdInActiveHierarchy(@Param("id") Long id);
}
