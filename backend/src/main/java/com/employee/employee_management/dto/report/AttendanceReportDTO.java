package com.employee.employee_management.dto.report;

import com.employee.employee_management.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportDTO {

    private Long id;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String designationName;
    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private BigDecimal workingHours;
    private BigDecimal overtime;
    private AttendanceStatus status;
    private String remarks;
}
