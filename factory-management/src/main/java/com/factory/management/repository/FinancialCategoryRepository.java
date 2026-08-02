package com.factory.management.repository;

import com.factory.management.entity.FinancialCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, Long> {
    List<FinancialCategory> findAllByActiveTrueOrderByCode();

    Optional<FinancialCategory> findByIdAndActiveTrue(Long id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
