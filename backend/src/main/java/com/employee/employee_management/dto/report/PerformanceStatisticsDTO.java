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
public class PerformanceStatisticsDTO {

    private BigDecimal averageRating;
    private long topPerformers;
    private long lowestPerformers;
    private long totalReviews;
}
