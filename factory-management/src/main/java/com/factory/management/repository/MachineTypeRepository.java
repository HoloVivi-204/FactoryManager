package com.factory.management.repository;

import com.factory.management.entity.MachineType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MachineTypeRepository extends JpaRepository<MachineType, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<MachineType> findAllByActiveTrue();

    Optional<MachineType> findByIdAndActiveTrue(Long id);
}
