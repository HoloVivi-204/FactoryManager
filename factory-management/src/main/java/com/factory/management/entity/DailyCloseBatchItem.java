package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "daily_close_batch_item", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_close_item_staging", columnNames = "staging_report_id"),
        @UniqueConstraint(name = "uk_daily_close_item_sequence", columnNames = {"close_batch_id", "sequence_no"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCloseBatchItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "close_batch_id", nullable = false, updatable = false)
    private DailyCloseBatch closeBatch;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staging_report_id", nullable = false, updatable = false)
    private ProductionReportStaging stagingReport;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "official_report_id", unique = true)
    private ProductionReport officialReport;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;
}
