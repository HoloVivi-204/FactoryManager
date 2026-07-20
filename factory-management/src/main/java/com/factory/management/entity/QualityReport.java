package com.factory.management.entity;
import jakarta.persistence.*;import lombok.*;
@Entity @Table(name="quality_report",uniqueConstraints=@UniqueConstraint(name="uk_quality_report_report_type",columnNames={"production_report_id","quality_error_type_id"})) @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QualityReport {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="production_report_id",nullable=false) ProductionReport productionReport;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="source_staging_id",nullable=false,unique=true) QualityReportStaging sourceStaging;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="quality_error_type_id",nullable=false) QualityErrorType qualityErrorType;
 @Column(nullable=false) Long quantity; @Column(length=1000) String description;
}
