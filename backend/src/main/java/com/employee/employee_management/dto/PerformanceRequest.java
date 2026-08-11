package com.employee.employee_management.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceRequest {

    @NotNull(message = "Employee is required")
    private Long employeeId;

    private Long reviewerId;

    @NotNull(message = "Review date is required")
    @PastOrPresent(message = "Review date cannot be in the future")
    private LocalDate reviewDate;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private String strengths;

    private String weaknesses;

    private String comments;
}
