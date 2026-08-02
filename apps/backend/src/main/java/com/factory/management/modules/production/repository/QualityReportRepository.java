package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.QualityReport;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityReportRepository extends JpaRepository<QualityReport, Long> {
    List<QualityReport> findAllByProductionReport_Id(Long id);

    List<QualityReport> findAllByProductionReport_IdIn(Collection<Long> ids);

    List<QualityReport> findAllByQualityErrorType_Id(Long id);
}
