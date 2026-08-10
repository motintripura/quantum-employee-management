package com.employee.employee_management.dto;

import com.employee.employee_management.entity.Gender;
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
public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private LocalDate joiningDate;
    private Long departmentId;
    private String departmentName;
    private Long designationId;
    private String designationName;
    private BigDecimal salary;
    private String profileImage;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String temporaryPassword;
}
