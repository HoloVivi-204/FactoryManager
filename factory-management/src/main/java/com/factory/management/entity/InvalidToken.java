package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
