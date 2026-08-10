package com.employee.employee_management.service;

import com.employee.employee_management.dto.DepartmentRequest;
import com.employee.employee_management.dto.DepartmentResponse;
import com.employee.employee_management.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {

    PageResponse<DepartmentResponse> getAll(String keyword, Boolean status, Pageable pageable);

    List<DepartmentResponse> getActiveList();

    DepartmentResponse getById(Long id);

    DepartmentResponse create(DepartmentRequest request);

    DepartmentResponse update(Long id, DepartmentRequest request);

    void delete(Long id);

    DepartmentResponse toggleStatus(Long id);
}
