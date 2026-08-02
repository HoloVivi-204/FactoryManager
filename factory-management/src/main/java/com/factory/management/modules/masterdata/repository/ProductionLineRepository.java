package com.factory.management.modules.masterdata.repository;

import com.factory.management.modules.masterdata.entity.ProductionLine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductionLineRepository extends JpaRepository<ProductionLine, Long> {
    boolean existsByDepartment_IdAndCodeIgnoreCase(Long departmentId, String code);

    boolean existsByDepartment_IdAndCodeIgnoreCaseAndIdNot(Long departmentId, String code, Long id);

    @Query("""
            select line
            from ProductionLine line
                join line.department department
                join department.factory factory
            where line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<ProductionLine> findAllActiveInActiveHierarchy();

    @Query("""
            select line
            from ProductionLine line
                join line.department department
                join department.factory factory
            where department.id = :departmentId
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<ProductionLine> findAllActiveByDepartmentIdInActiveHierarchy(
            @Param("departmentId") Long departmentId
    );

    @Query("""
            select line
            from ProductionLine line
                join line.department department
                join department.factory factory
            where line.id = :id
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    Optional<ProductionLine> findActiveByIdInActiveHierarchy(@Param("id") Long id);
}
