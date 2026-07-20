package com.factory.management.repository;

import com.factory.management.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Material> findAllByActiveTrue();

    Optional<Material> findByIdAndActiveTrue(Long id);
}
