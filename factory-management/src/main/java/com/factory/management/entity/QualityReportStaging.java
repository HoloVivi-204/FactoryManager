package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "quality_report_staging",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_quality_report_staging_report_error_type",
                columnNames = {"production_report_staging_id", "quality_error_type_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QualityReportStaging {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_report_staging_id", nullable = false)
    private ProductionReportStaging productionReportStaging;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quality_error_type_id", nullable = false)
    private QualityErrorType qualityErrorType;

    @Column(nullable = false)
    private Long quantity;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
