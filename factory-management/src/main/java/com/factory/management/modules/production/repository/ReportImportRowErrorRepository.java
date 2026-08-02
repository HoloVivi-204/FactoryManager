package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.ReportImportRowError;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportImportRowErrorRepository extends JpaRepository<ReportImportRowError, Long> {
    List<ReportImportRowError> findAllByImportBatch_IdOrderBySheetNameAscRowNumberAscIdAsc(Long importBatchId);
    void deleteAllByImportBatch_Id(Long importBatchId);
}
