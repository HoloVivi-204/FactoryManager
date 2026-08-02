package com.factory.management.repository;

import com.factory.management.entity.DailyCloseBatch;
import com.factory.management.entity.DailyCloseBatchStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyCloseBatchRepository extends JpaRepository<DailyCloseBatch, Long> {
    Optional<DailyCloseBatch> findByBusinessKey(String businessKey);
    List<DailyCloseBatch> findAllByOrderByCreatedAtDesc();
    List<DailyCloseBatch> findAllByStatusOrderByCreatedAtDesc(DailyCloseBatchStatus status);
}
