package com.factory.management.modules.auth.repository;

import com.factory.management.modules.auth.entity.InvalidToken;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {
    void deleteAllByExpiryTimeBefore(LocalDateTime time);
}
