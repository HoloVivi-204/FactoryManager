package com.factory.management.repository;

import com.factory.management.entity.ProductionLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {
    boolean existsByDepartment_IdAndCodeIgnoreCase(Long departmentId, String code);

    boolean existsByDepartment_IdAndCodeIgnoreCaseAndIdNot(Long departmentId, String code, Long id);

    List<ProductionLine> findAllByActiveTrueAndDepartment_ActiveTrueAndDepartment_Factory_ActiveTrue();

    List<ProductionLine> findAllByDepartment_IdAndActiveTrueAndDepartment_ActiveTrueAndDepartment_Factory_ActiveTrue(
            Long departmentId
    );

    Optional<ProductionLine> findByIdAndActiveTrueAndDepartment_ActiveTrueAndDepartment_Factory_ActiveTrue(Long id);
}
