package com.employee.employee_management.dto.report;

import com.employee.employee_management.entity.Gender;
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
public class EmployeeReportDTO {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Gender gender;
    private String departmentName;
    private String designationName;
    private LocalDate joiningDate;
    private BigDecimal salary;
    private Boolean status;
    private Integer age;
    private String address;
}
