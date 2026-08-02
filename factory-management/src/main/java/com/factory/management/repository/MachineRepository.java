package com.factory.management.repository;

import com.factory.management.entity.Machine;
import com.factory.management.entity.MachineOperationalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsBySerialNumberIgnoreCase(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);

    @Query("""
            select machine
            from Machine machine
                join machine.machineType machineType
                join machine.team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where machine.active = true
                and machineType.active = true
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<Machine> findAllActiveInActiveHierarchy();

    @Query("""
            select machine
            from Machine machine
                join machine.machineType machineType
                join machine.team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where team.id = :teamId
                and machine.active = true
                and machineType.active = true
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<Machine> findAllActiveByTeamIdInActiveHierarchy(@Param("teamId") Long teamId);

    @Query("""
            select machine
            from Machine machine
                join machine.machineType machineType
                join machine.team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where machineType.id = :machineTypeId
                and machine.active = true
                and machineType.active = true
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<Machine> findAllActiveByMachineTypeIdInActiveHierarchy(
            @Param("machineTypeId") Long machineTypeId
    );

    @Query("""
            select machine
            from Machine machine
                join machine.machineType machineType
                join machine.team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where machine.operationalStatus = :operationalStatus
                and machine.active = true
                and machineType.active = true
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    List<Machine> findAllActiveByOperationalStatusInActiveHierarchy(
            @Param("operationalStatus") MachineOperationalStatus operationalStatus
    );

    @Query("""
            select machine
            from Machine machine
                join machine.machineType machineType
                join machine.team team
                join team.productionLine line
                join line.department department
                join department.factory factory
            where machine.id = :id
                and machine.active = true
                and machineType.active = true
                and team.active = true
                and line.active = true
                and department.active = true
                and factory.active = true
            """)
    Optional<Machine> findActiveByIdInActiveHierarchy(@Param("id") Long id);
}
