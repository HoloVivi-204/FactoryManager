package com.factory.management.repository;

import com.factory.management.entity.ReportImportRowError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportImportRowErrorRepository extends JpaRepository<ReportImportRowError, Long> {
    List<ReportImportRowError> findAllByImportBatch_IdOrderBySheetNameAscRowNumberAscIdAsc(Long importBatchId);
    void deleteAllByImportBatch_Id(Long importBatchId);
}
