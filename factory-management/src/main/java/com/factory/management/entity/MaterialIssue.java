package com.factory.management.entity;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;
@Entity @Table(name="material_issue") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaterialIssue {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="production_report_id",nullable=false) ProductionReport productionReport;
 @OneToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="source_staging_id",nullable=false,unique=true) MaterialIssueStaging sourceStaging;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="material_id",nullable=false) Material material;
 @Enumerated(EnumType.STRING) @Column(name="issue_type",nullable=false,length=30) MaterialIssueType issueType;
 @Column(nullable=false,precision=19,scale=3) BigDecimal quantity; @Column(nullable=false,length=50) String unit; @Column(length=1000) String description;
}
