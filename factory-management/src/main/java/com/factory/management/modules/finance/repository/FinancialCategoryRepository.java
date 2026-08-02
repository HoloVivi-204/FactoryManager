package com.factory.management.modules.finance.repository;

import com.factory.management.modules.finance.entity.FinancialCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialCategoryRepository extends JpaRepository<FinancialCategory, Long> {
    List<FinancialCategory> findAllByActiveTrueOrderByCode();

    Optional<FinancialCategory> findByIdAndActiveTrue(Long id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
}
