package com.factory.management.repository;

import com.factory.management.entity.DowntimeReason;
import com.factory.management.entity.DowntimeReasonType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DowntimeReasonRepository extends JpaRepository<DowntimeReason, Long> {
    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<DowntimeReason> findAllByActiveTrue();

    List<DowntimeReason> findAllByReasonTypeAndActiveTrue(DowntimeReasonType reasonType);

    Optional<DowntimeReason> findByIdAndActiveTrue(Long id);
}
