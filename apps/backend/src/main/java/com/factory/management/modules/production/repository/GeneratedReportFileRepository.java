package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.GeneratedReportFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedReportFileRepository extends JpaRepository<GeneratedReportFile, Long> {
    List<GeneratedReportFile> findAllByCloseBatch_IdOrderByFileVersionDesc(Long closeBatchId);
    Optional<GeneratedReportFile> findTopByCloseBatch_IdOrderByFileVersionDesc(Long closeBatchId);
}
