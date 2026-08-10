package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.DepartmentResponse;
import com.employee.employee_management.entity.Department;

public final class DepartmentMapper {

    private DepartmentMapper() {
    }

    public static DepartmentResponse toResponse(Department department, long employeeCount) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .status(department.getStatus())
                .employeeCount(employeeCount)
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .build();
    }

    public static DepartmentResponse toResponse(Department department) {
        return toResponse(department, 0);
    }
}
