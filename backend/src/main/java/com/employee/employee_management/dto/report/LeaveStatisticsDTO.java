package com.employee.employee_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveStatisticsDTO {

    private long totalLeaveRequests;
    private long pending;
    private long approved;
    private long rejected;
    private long cancelled;
    private BigDecimal totalLeaveDays;
}
