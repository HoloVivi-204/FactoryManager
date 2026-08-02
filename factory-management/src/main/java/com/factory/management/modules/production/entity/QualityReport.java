package com.factory.management.modules.production.entity;

import com.factory.management.modules.masterdata.entity.QualityErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "quality_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_quality_report_report_type",
                columnNames = {"production_report_id", "quality_error_type_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualityReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_report_id", nullable = false)
    private ProductionReport productionReport;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_staging_id", nullable = false, unique = true)
    private QualityReportStaging sourceStaging;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quality_error_type_id", nullable = false)
    private QualityErrorType qualityErrorType;

    @Column(nullable = false)
    private Long quantity;

    @Column(length = 1000)
    private String description;
}
