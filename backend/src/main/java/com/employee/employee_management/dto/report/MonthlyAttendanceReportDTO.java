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
public class MonthlyAttendanceReportDTO {

    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private long workingDays;
    private long present;
    private long absent;
    private long late;
    private long halfDay;
    private long leaveDays;
    private BigDecimal totalWorkingHours;
    private BigDecimal totalOvertime;
    private BigDecimal attendancePercentage;
}
