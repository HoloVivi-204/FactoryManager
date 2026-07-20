package com.factory.management.repository;

import com.factory.management.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

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
