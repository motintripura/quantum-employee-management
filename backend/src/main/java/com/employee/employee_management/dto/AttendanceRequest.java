package com.employee.employee_management.dto;

import com.employee.employee_management.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    @NotNull(message = "Employee is required")
    private Long employeeId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    private LocalTime checkIn;

    private LocalTime checkOut;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String remarks;
}
