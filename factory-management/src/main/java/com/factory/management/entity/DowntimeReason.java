package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
        name = "downtime_reason",
        uniqueConstraints = @UniqueConstraint(name = "uk_downtime_reason_code", columnNames = "code")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DowntimeReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 20)
    @ColumnDefault("'UNPLANNED'")
    @Builder.Default
    private DowntimeReasonType reasonType = DowntimeReasonType.UNPLANNED;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
