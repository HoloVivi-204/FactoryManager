package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        name = "quality_error_type",
        uniqueConstraints = @UniqueConstraint(name = "uk_quality_error_type_code", columnNames = "code")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityErrorType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'MEDIUM'")
    @Builder.Default
    private QualityErrorSeverity severity = QualityErrorSeverity.MEDIUM;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
