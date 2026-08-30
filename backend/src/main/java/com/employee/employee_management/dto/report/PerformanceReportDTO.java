package com.employee.employee_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceReportDTO {

    private Long id;
    private String employeeCode;
    private String employeeName;
    private String departmentName;
    private String designationName;
    private String reviewerName;
    private LocalDate reviewDate;
    private Integer rating;
    private String strengths;
    private String weaknesses;
    private String comments;
}
