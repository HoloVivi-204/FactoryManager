package com.factory.management.repository;

import com.factory.management.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository
        extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    List<Notification> findAllByRecipient_IdOrderByCreatedAtDesc(Long id);
    Optional<Notification> findByIdAndRecipient_Id(Long id, Long employeeId);
    long countByRecipient_IdAndReadFalse(Long id);
}
