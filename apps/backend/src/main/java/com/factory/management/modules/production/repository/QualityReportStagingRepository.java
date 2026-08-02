package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.QualityReportStaging;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QualityReportStagingRepository extends JpaRepository<QualityReportStaging, Long> {
    boolean existsByProductionReportStaging_IdAndQualityErrorType_Id(Long reportId, Long errorTypeId);

    boolean existsByProductionReportStaging_IdAndQualityErrorType_IdAndIdNot(Long reportId, Long errorTypeId, Long id);

    List<QualityReportStaging> findAllByActiveTrue();

    List<QualityReportStaging> findAllByProductionReportStaging_IdAndActiveTrue(Long reportId);

    List<QualityReportStaging> findAllByProductionReportStaging_IdInAndActiveTrue(Collection<Long> reportIds);

    List<QualityReportStaging> findAllByQualityErrorType_IdAndActiveTrue(Long errorTypeId);

    Optional<QualityReportStaging> findByIdAndActiveTrue(Long id);

    Optional<QualityReportStaging> findByProductionReportStaging_IdAndQualityErrorType_Id(Long reportId, Long errorTypeId);

    long deleteAllByProductionReportStaging_Id(Long reportId);

    @Query("""
            select coalesce(sum(q.quantity), 0)
            from QualityReportStaging q
            where q.productionReportStaging.id = :reportId
                and q.active = true
            """)
    Long sumActiveQuantityByReportId(@Param("reportId") Long reportId);

    @Query("""
            select coalesce(sum(q.quantity), 0)
            from QualityReportStaging q
            where q.productionReportStaging.id = :reportId
                and q.active = true
                and q.id <> :excludedId
            """)
    Long sumActiveQuantityByReportIdExcludingId(@Param("reportId") Long reportId, @Param("excludedId") Long excludedId);
}
