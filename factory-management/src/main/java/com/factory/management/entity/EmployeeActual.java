package com.factory.management.entity;
import jakarta.persistence.*;import lombok.*;
@Entity @Table(name="employee_actual",uniqueConstraints=@UniqueConstraint(name="uk_employee_actual_report_employee",columnNames={"production_report_id","employee_id"})) @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeActual {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="production_report_id",nullable=false) ProductionReport productionReport;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="source_staging_id",nullable=false,unique=true) EmployeeActualStaging sourceStaging;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="employee_id",nullable=false) Employee employee;
 @Column(name="working_minutes",nullable=false) Integer workingMinutes; @Column(name="overtime_minutes",nullable=false) Integer overtimeMinutes;
 @Enumerated(EnumType.STRING) @Column(name="attendance_status",nullable=false,length=30) AttendanceStatus attendanceStatus;
 @Enumerated(EnumType.STRING) @Column(name="assignment_type",nullable=false,length=30) AssignmentType assignmentType;
 @Column(length=1000) String description;
}
