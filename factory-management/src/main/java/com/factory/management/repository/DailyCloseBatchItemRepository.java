package com.factory.management.repository;

import com.factory.management.entity.DailyCloseBatchItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyCloseBatchItemRepository extends JpaRepository<DailyCloseBatchItem, Long> {
    boolean existsByStagingReport_Id(Long stagingReportId);
    List<DailyCloseBatchItem> findAllByCloseBatch_IdOrderBySequenceNo(Long closeBatchId);
}
