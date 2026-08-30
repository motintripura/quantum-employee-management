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
public class LeaveBalanceReportDTO {

    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String leaveTypeName;
    private Integer year;
    private BigDecimal allocatedDays;
    private BigDecimal usedDays;
    private BigDecimal remainingDays;
}
