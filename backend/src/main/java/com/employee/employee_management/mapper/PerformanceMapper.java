package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.PerformanceResponse;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.PerformanceReview;

public final class PerformanceMapper {

    private PerformanceMapper() {
    }

    public static PerformanceResponse toResponse(PerformanceReview review) {
        Employee employee = review.getEmployee();
        String name = employee.getLastName() == null || employee.getLastName().isBlank()
                ? employee.getFirstName()
                : employee.getFirstName() + " " + employee.getLastName();
        return PerformanceResponse.builder()
                .id(review.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(name)
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getName())
                .reviewerId(review.getReviewer() == null ? null : review.getReviewer().getId())
                .reviewerUsername(review.getReviewer() == null ? null : review.getReviewer().getUsername())
                .reviewDate(review.getReviewDate())
                .rating(review.getRating())
                .strengths(review.getStrengths())
                .weaknesses(review.getWeaknesses())
                .comments(review.getComments())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
