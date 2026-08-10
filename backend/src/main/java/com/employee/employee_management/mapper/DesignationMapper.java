package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.DesignationResponse;
import com.employee.employee_management.entity.Designation;

public final class DesignationMapper {

    private DesignationMapper() {
    }

    public static DesignationResponse toResponse(Designation designation) {
        return DesignationResponse.builder()
                .id(designation.getId())
                .name(designation.getName())
                .description(designation.getDescription())
                .departmentId(designation.getDepartment().getId())
                .departmentName(designation.getDepartment().getName())
                .status(designation.getStatus())
                .createdAt(designation.getCreatedAt())
                .updatedAt(designation.getUpdatedAt())
                .build();
    }
}
