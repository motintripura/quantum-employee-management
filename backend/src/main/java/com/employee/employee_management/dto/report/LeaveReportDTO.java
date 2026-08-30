package com.employee.employee_management.dto.report;

import com.employee.employee_management.entity.LeaveRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveReportDTO {

    private Long id;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;
    private LeaveRequestStatus status;
    private String approvedByName;
    private LocalDateTime approvedAt;
}
