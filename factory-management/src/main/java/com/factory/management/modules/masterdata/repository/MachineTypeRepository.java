package com.factory.management.modules.masterdata.repository;

import com.factory.management.modules.masterdata.entity.MachineType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineTypeRepository extends JpaRepository<MachineType, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<MachineType> findAllByActiveTrue();

    Optional<MachineType> findByIdAndActiveTrue(Long id);
}
