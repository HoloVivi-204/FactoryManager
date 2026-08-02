package com.factory.management.repository;

import com.factory.management.entity.FinancialPayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialPaymentRepository extends JpaRepository<FinancialPayment, Long> {
    List<FinancialPayment> findAllByFinancialRecord_IdOrderByPaymentDateDescCreatedAtDesc(Long recordId);
}
