package com.factory.management.repository;

import com.factory.management.entity.QualityErrorSeverity;
import com.factory.management.entity.QualityErrorType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityErrorTypeRepository extends JpaRepository<QualityErrorType, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<QualityErrorType> findAllByActiveTrue();

    List<QualityErrorType> findAllBySeverityAndActiveTrue(QualityErrorSeverity severity);

    Optional<QualityErrorType> findByIdAndActiveTrue(Long id);
}
