package com.factory.management.repository;

import com.factory.management.entity.ReportImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportImportBatchRepository extends JpaRepository<ReportImportBatch, Long> {
    Optional<ReportImportBatch> findByCloseBatch_IdAndFileHashSha256(Long closeBatchId, String fileHashSha256);
    List<ReportImportBatch> findAllByCloseBatch_IdOrderByUploadedAtDesc(Long closeBatchId);
}
