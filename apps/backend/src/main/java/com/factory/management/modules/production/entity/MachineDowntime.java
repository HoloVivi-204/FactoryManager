package com.factory.management.modules.production.entity;

import com.factory.management.modules.masterdata.entity.DowntimeReason;
import com.factory.management.modules.masterdata.entity.Machine;
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
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "machine_downtime")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineDowntime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_report_id", nullable = false)
    private ProductionReport productionReport;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_staging_id", nullable = false, unique = true)
    private MachineDowntimeStaging sourceStaging;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "downtime_reason_id", nullable = false)
    private DowntimeReason downtimeReason;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(length = 1000)
    private String description;
}
