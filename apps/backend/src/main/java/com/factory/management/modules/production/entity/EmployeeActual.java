package com.factory.management.modules.production.entity;

import com.factory.management.modules.hr.entity.AttendanceStatus;
import com.factory.management.modules.masterdata.entity.AssignmentType;
import com.factory.management.modules.masterdata.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "employee_actual",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_actual_report_employee",
                columnNames = {"production_report_id", "employee_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeActual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_report_id", nullable = false)
    private ProductionReport productionReport;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_staging_id", nullable = false, unique = true)
    private EmployeeActualStaging sourceStaging;

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
}
