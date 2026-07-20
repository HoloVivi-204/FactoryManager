package com.factory.management.repository;

import com.factory.management.entity.InvalidToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {
    void deleteAllByExpiryTimeBefore(LocalDateTime time);
}
