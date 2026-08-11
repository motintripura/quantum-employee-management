package com.employee.employee_management.service;

import com.employee.employee_management.dto.PerformanceRequest;
import com.employee.employee_management.dto.PerformanceResponse;
import com.employee.employee_management.dto.StatusCountResponse;
import com.employee.employee_management.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PerformanceService {

    PageResponse<PerformanceResponse> getAll(Long employeeId, Long reviewerId, Integer rating, Pageable pageable);

    List<PerformanceResponse> getEmployeePerformance(Long employeeId);

    PerformanceResponse getById(Long id);

    PerformanceResponse create(PerformanceRequest request);

    PerformanceResponse update(Long id, PerformanceRequest request);

    void delete(Long id);

    List<StatusCountResponse> ratingReport();
}
