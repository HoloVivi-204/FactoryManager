package com.factory.management.repository;

import com.factory.management.entity.QualityReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface QualityReportRepository extends JpaRepository<QualityReport, Long> {
    List<QualityReport> findAllByProductionReport_Id(Long id);

    List<QualityReport> findAllByProductionReport_IdIn(Collection<Long> ids);

    List<QualityReport> findAllByQualityErrorType_Id(Long id);
}
