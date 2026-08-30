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
public class DepartmentPayrollReportDTO {

    private String departmentName;
    private long employeeCount;
    private BigDecimal totalBasicSalary;
    private BigDecimal totalAllowances;
    private BigDecimal totalOvertime;
    private BigDecimal totalBonus;
    private BigDecimal totalDeduction;
    private BigDecimal totalNetSalary;
}
