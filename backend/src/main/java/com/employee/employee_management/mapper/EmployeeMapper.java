package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.EmployeeResponse;
import com.employee.employee_management.entity.Employee;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(fullName(employee))
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .dateOfBirth(employee.getDateOfBirth())
                .gender(employee.getGender())
                .address(employee.getAddress())
                .joiningDate(employee.getJoiningDate())
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getName())
                .designationId(employee.getDesignation().getId())
                .designationName(employee.getDesignation().getName())
                .salary(employee.getSalary())
                .profileImage(employee.getProfileImage())
                .status(employee.getStatus())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    private static String fullName(Employee employee) {
        return employee.getLastName() == null || employee.getLastName().isBlank()
                ? employee.getFirstName()
                : employee.getFirstName() + " " + employee.getLastName();
    }
}
