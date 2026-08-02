package com.factory.management.modules.masterdata.repository;

import com.factory.management.modules.masterdata.entity.Shift;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShiftRepository extends JpaRepository<Shift, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Shift> findAllByActiveTrue();

    Optional<Shift> findByIdAndActiveTrue(Long id);
}
