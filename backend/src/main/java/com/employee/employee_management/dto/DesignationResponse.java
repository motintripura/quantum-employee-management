package com.employee.employee_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DesignationResponse {

    private Long id;
    private String name;
    private String description;
    private Long departmentId;
    private String departmentName;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
