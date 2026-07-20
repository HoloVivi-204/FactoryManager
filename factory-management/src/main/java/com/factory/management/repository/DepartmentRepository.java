package com.factory.management.repository;

import com.factory.management.entity.Department;
import com.factory.management.entity.DepartmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByFactory_IdAndCodeIgnoreCase(Long factoryId, String code);

    boolean existsByFactory_IdAndCodeIgnoreCaseAndIdNot(Long factoryId, String code, Long id);

    boolean existsByFactory_IdAndDepartmentType(Long factoryId, DepartmentType departmentType);

    boolean existsByFactory_IdAndDepartmentTypeAndIdNot(
            Long factoryId,
            DepartmentType departmentType,
            Long id
    );

    List<Department> findAllByActiveTrueAndFactory_ActiveTrue();

    List<Department> findAllByFactory_IdAndActiveTrueAndFactory_ActiveTrue(Long factoryId);

    Optional<Department> findByIdAndActiveTrueAndFactory_ActiveTrue(Long id);

    Optional<Department> findByIdAndFactory_ActiveTrue(Long id);
}
