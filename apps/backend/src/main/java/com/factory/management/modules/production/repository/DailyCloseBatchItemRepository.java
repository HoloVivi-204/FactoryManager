package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.DailyCloseBatchItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyCloseBatchItemRepository extends JpaRepository<DailyCloseBatchItem, Long> {
    boolean existsByStagingReport_Id(Long stagingReportId);
    List<DailyCloseBatchItem> findAllByCloseBatch_IdOrderBySequenceNo(Long closeBatchId);
}
