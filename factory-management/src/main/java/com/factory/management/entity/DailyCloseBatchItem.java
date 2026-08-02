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
