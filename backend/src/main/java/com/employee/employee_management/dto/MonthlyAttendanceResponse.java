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
public class MonthlyAttendanceResponse {

    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long departmentId;
    private String departmentName;
    private int year;
    private int month;
    private long presentDays;
    private long absentDays;
    private long lateDays;
    private long halfDays;
    private long leaveDays;
    private long totalDays;
    private BigDecimal totalWorkingHours;
}
