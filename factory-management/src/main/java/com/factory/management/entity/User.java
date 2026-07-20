package com.factory.management.entity;
import jakarta.persistence.*;import lombok.*;import java.time.LocalDateTime;import java.util.*;
@Entity @Table(name="app_user",uniqueConstraints={@UniqueConstraint(name="uk_user_username",columnNames="username"),@UniqueConstraint(name="uk_user_employee",columnNames="employee_id")})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="employee_id",nullable=false) private Employee employee;
 @Column(nullable=false,length=100) private String username;
 @Column(name="password_hash",nullable=false,length=255) private String passwordHash;
 @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="user_role",joinColumns=@JoinColumn(name="user_id")) @Enumerated(EnumType.STRING) @Column(name="role",nullable=false,length=40) @Builder.Default private Set<Role> roles=new HashSet<>();
 @Column(nullable=false) @Builder.Default private Boolean enabled=true;
 @Column(name="account_non_locked",nullable=false) @Builder.Default private Boolean accountNonLocked=true;
 @Column(name="last_login_at") private LocalDateTime lastLoginAt;
 @Column(name="token_version",nullable=false) @Builder.Default private Long tokenVersion=0L;
 @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 @PrePersist void create(){createdAt=updatedAt=LocalDateTime.now();}@PreUpdate void update(){updatedAt=LocalDateTime.now();}
}
