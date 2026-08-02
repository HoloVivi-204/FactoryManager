package com.factory.management.repository;

import com.factory.management.entity.InvalidToken;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {
    void deleteAllByExpiryTimeBefore(LocalDateTime time);
}
