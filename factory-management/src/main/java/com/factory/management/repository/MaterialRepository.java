package com.factory.management.repository;

import com.factory.management.entity.Material;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Material> findAllByActiveTrue();

    Optional<Material> findByIdAndActiveTrue(Long id);
}
