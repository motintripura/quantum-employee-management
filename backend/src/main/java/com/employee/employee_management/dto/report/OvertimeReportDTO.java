package com.employee.employee_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OvertimeReportDTO {

    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private LocalDate attendanceDate;
    private BigDecimal overtimeHours;
}
