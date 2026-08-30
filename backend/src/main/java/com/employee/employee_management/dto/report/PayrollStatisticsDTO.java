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
public class PayrollStatisticsDTO {

    private long totalPayroll;
    private BigDecimal totalGrossSalary;
    private BigDecimal totalNetSalary;
    private BigDecimal totalTax;
    private BigDecimal totalDeduction;
    private BigDecimal totalBonus;
    private BigDecimal totalOvertime;
    private long paidPayroll;
    private long pendingPayroll;
}
