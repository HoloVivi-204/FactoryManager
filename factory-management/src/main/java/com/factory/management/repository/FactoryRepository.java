package com.factory.management.repository;

import com.factory.management.entity.Factory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FactoryRepository extends JpaRepository<Factory, Long> {
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    List<Factory> findAllByActiveTrue();
    Optional<Factory> findByIdAndActiveTrue(Long id);
}


