package com.employee.employee_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private long totalDepartments;
    private long totalDesignations;
    private long totalLeaveTypes;
    private long todayPresent;
    private long todayAbsent;
    private long todayLate;
    private long todayHalfDay;
    private long todayLeave;
    private long pendingLeaveRequests;
    private BigDecimal monthlyPayrollTotal;
}
