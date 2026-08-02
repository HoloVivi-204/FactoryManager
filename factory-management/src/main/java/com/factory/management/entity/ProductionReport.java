package com.factory.management.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_report", uniqueConstraints = {
        @UniqueConstraint(name = "uk_production_report_no", columnNames = "report_no"),
        @UniqueConstraint(name = "uk_production_report_source_staging", columnNames = "source_staging_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_no", nullable = false, length = 30)
    private String reportNo;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_staging_id", nullable = false, updatable = false)
    private ProductionReportStaging sourceStaging;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_line_id", nullable = false)
    private ProductionLine productionLine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_employee_id", nullable = false)
    private Employee leaderEmployee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Column(name = "planned_quantity", nullable = false)
    private Long plannedQuantity;

    @Column(name = "actual_quantity", nullable = false)
    private Long actualQuantity;

    @Column(name = "good_quantity", nullable = false)
    private Long goodQuantity;

    @Column(name = "defect_quantity", nullable = false)
    private Long defectQuantity;

    @Column(name = "working_minutes", nullable = false)
    private Integer workingMinutes;

    @Column(name = "downtime_minutes", nullable = false)
    private Integer downtimeMinutes;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal availability;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal performance;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal quality;

    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal oee;

    @Column(length = 1000)
    private String remark;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approved_by", nullable = false)
    private Employee approvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;
}
