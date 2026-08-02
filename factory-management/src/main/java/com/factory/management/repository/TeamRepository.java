package com.factory.management.repository;

import com.factory.management.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByProductionLine_IdAndCodeIgnoreCase(Long productionLineId, String code);

    boolean existsByProductionLine_IdAndCodeIgnoreCaseAndIdNot(
            Long productionLineId,
            String code,
            Long id
    );

    @Query("""
            select team
            from Team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<Team> findAllActiveInActiveHierarchy();

    @Query("""
            select team
            from Team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where line.id = :productionLineId
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<Team> findAllActiveByProductionLineIdInActiveHierarchy(
            @Param("productionLineId") Long productionLineId
    );

    @Query("""
            select team
            from Team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where team.id = :id
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    Optional<Team> findActiveByIdInActiveHierarchy(@Param("id") Long id);

    @Query("""
            select team
            from Team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where team.id = :id
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    Optional<Team> findByIdInActiveHierarchy(@Param("id") Long id);

    Optional<Team> findByLeader_Id(Long employeeId);
}
