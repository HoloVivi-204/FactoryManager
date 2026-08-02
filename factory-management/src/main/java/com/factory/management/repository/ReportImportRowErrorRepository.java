package com.factory.management.repository;

import com.factory.management.entity.ReportImportRowError;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportImportRowErrorRepository extends JpaRepository<ReportImportRowError, Long> {
    List<ReportImportRowError> findAllByImportBatch_IdOrderBySheetNameAscRowNumberAscIdAsc(Long importBatchId);
    void deleteAllByImportBatch_Id(Long importBatchId);
}
