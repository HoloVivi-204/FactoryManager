package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_data_scope", uniqueConstraints = @UniqueConstraint(
        name = "uk_user_data_scope", columnNames = {"user_id", "scope_type", "scope_id"}))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDataScope {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @Enumerated(EnumType.STRING) @Column(name = "scope_type", nullable = false, length = 30) private DataScopeType scopeType;
    @Column(name = "scope_id", nullable = false) private Long scopeId;
}
