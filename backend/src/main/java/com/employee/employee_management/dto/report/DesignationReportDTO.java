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
public class DesignationReportDTO {

    private Long id;
    private String name;
    private String departmentName;
    private Boolean status;
    private long employeeCount;
    private BigDecimal totalSalary;
}
