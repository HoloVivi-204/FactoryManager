package com.factory.management.repository;

import com.factory.management.entity.FinancialPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinancialPaymentRepository extends JpaRepository<FinancialPayment, Long> {
    List<FinancialPayment> findAllByFinancialRecord_IdOrderByPaymentDateDescCreatedAtDesc(Long recordId);
}
