package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.MaterialIssueStaging;
import com.factory.management.modules.production.entity.MaterialIssueType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialIssueStagingRepository extends JpaRepository<MaterialIssueStaging, Long> {
    List<MaterialIssueStaging> findAllByActiveTrue();
    List<MaterialIssueStaging> findAllByProductionReportStaging_IdAndActiveTrue(Long reportId);
    List<MaterialIssueStaging> findAllByProductionReportStaging_IdInAndActiveTrue(Collection<Long> reportIds);
    List<MaterialIssueStaging> findAllByMaterial_IdAndActiveTrue(Long materialId);
    List<MaterialIssueStaging> findAllByIssueTypeAndActiveTrue(MaterialIssueType issueType);
    Optional<MaterialIssueStaging> findByIdAndActiveTrue(Long id);
    Optional<MaterialIssueStaging> findFirstByProductionReportStaging_IdAndMaterial_IdAndIssueTypeOrderByIdAsc(
            Long reportId, Long materialId, MaterialIssueType issueType);
    long deleteAllByProductionReportStaging_Id(Long reportId);
}
