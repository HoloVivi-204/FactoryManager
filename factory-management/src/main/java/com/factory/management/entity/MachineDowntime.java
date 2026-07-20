package com.factory.management.entity;
import jakarta.persistence.*;import lombok.*;import java.time.LocalDateTime;
@Entity @Table(name="machine_downtime") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MachineDowntime {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="production_report_id",nullable=false) ProductionReport productionReport;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="source_staging_id",nullable=false,unique=true) MachineDowntimeStaging sourceStaging;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="machine_id",nullable=false) Machine machine;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="downtime_reason_id",nullable=false) DowntimeReason downtimeReason;
 @Column(name="start_time",nullable=false) LocalDateTime startTime; @Column(name="end_time",nullable=false) LocalDateTime endTime;
 @Column(name="duration_minutes",nullable=false) Integer durationMinutes; @Column(length=1000) String description;
}
