package com.factory.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invalid_token")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvalidToken {
    @Id
    @Column(length = 100)
    private String id;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
}
