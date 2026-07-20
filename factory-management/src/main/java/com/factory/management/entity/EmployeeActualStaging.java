package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "employee_actual_staging",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_actual_staging_report_employee",
                columnNames = {"production_report_staging_id", "employee_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeActualStaging {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_report_staging_id", nullable = false)
    private ProductionReportStaging productionReportStaging;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "working_minutes", nullable = false)
    private Integer workingMinutes;

    @Column(name = "overtime_minutes", nullable = false)
    private Integer overtimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 30)
    private AttendanceStatus attendanceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 30)
    private AssignmentType assignmentType;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
