package com.factory.management.dto.response;

import com.factory.management.entity.DailyCloseBatchStatus;
import com.factory.management.entity.DataScopeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCloseBatchResponse {
    private Long id;
    private Long version;
    private String batchNo;
    private LocalDate reportDate;
    private DataScopeType scopeType;
    private Long scopeId;
    private String scopeCode;
    private String scopeName;
    private Long shiftId;
    private String shiftCode;
    private String shiftName;
    private DailyCloseBatchStatus status;
    private Integer reportCount;
    private Integer latestFileVersion;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime closedAt;
    private String closedBy;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private LocalDateTime importedAt;
    private String importedBy;
    private LocalDateTime lockedAt;
    private String lockedBy;
    private LocalDateTime updatedAt;
    private List<DailyCloseBatchItemResponse> reports;
}
