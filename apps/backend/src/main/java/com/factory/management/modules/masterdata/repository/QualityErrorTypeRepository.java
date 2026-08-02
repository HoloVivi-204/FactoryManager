package com.factory.management.modules.masterdata.repository;

import com.factory.management.modules.masterdata.entity.QualityErrorSeverity;
import com.factory.management.modules.masterdata.entity.QualityErrorType;
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
