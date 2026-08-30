package com.employee.employee_management.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeStatisticsDTO {

    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private long newEmployeesThisMonth;
    private List<Object[]> employeesByDepartment;
    private List<Object[]> employeesByDesignation;
}
