package com.factory.management.repository;

import com.factory.management.entity.DailyCloseBatchItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyCloseBatchItemRepository extends JpaRepository<DailyCloseBatchItem, Long> {
    boolean existsByStagingReport_Id(Long stagingReportId);
    List<DailyCloseBatchItem> findAllByCloseBatch_IdOrderBySequenceNo(Long closeBatchId);
}
