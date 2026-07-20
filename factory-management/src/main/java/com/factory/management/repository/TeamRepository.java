package com.factory.management.repository;

import com.factory.management.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByProductionLine_IdAndCodeIgnoreCase(Long productionLineId, String code);

    boolean existsByProductionLine_IdAndCodeIgnoreCaseAndIdNot(
            Long productionLineId,
            String code,
            Long id
    );

    List<Team> findAllByActiveTrueAndProductionLine_ActiveTrueAndProductionLine_Department_ActiveTrueAndProductionLine_Department_Factory_ActiveTrue();

    List<Team> findAllByProductionLine_IdAndActiveTrueAndProductionLine_ActiveTrueAndProductionLine_Department_ActiveTrueAndProductionLine_Department_Factory_ActiveTrue(
            Long productionLineId
    );

    Optional<Team> findByIdAndActiveTrueAndProductionLine_ActiveTrueAndProductionLine_Department_ActiveTrueAndProductionLine_Department_Factory_ActiveTrue(
            Long id
    );

    Optional<Team> findByIdAndProductionLine_ActiveTrueAndProductionLine_Department_ActiveTrueAndProductionLine_Department_Factory_ActiveTrue(
            Long id
    );

    Optional<Team> findByLeader_Id(Long employeeId);
}
