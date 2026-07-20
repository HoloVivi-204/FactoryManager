package com.factory.management.repository;

import com.factory.management.entity.GeneratedReportFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GeneratedReportFileRepository extends JpaRepository<GeneratedReportFile, Long> {
    List<GeneratedReportFile> findAllByCloseBatch_IdOrderByFileVersionDesc(Long closeBatchId);
    Optional<GeneratedReportFile> findTopByCloseBatch_IdOrderByFileVersionDesc(Long closeBatchId);
}
