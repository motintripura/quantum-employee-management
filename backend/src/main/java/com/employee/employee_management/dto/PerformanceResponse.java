package com.employee.employee_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long departmentId;
    private String departmentName;
    private Long reviewerId;
    private String reviewerUsername;
    private LocalDate reviewDate;
    private Integer rating;
    private String strengths;
    private String weaknesses;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
