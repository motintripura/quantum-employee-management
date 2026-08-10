package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.UserResponse;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        String employeeName = null;
        Long employeeId = null;
        if (user.getEmployee() != null) {
            Employee employee = user.getEmployee();
            employeeId = employee.getId();
            employeeName = employee.getFirstName()
                    + (employee.getLastName() != null ? " " + employee.getLastName() : "");
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .employeeId(employeeId)
                .employeeName(employeeName)
                .enabled(user.getEnabled())
                .build();
    }
}
