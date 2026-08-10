package com.employee.employee_management.service;

import com.employee.employee_management.dto.EmployeeRequest;
import com.employee.employee_management.dto.EmployeeResponse;
import com.employee.employee_management.util.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {

    PageResponse<EmployeeResponse> getAll(String keyword,
                                          Long departmentId,
                                          Long designationId,
                                          Boolean status,
                                          Pageable pageable);

    EmployeeResponse getById(Long id);

    EmployeeResponse create(EmployeeRequest request);

    EmployeeResponse update(Long id, EmployeeRequest request);

    void delete(Long id);

    EmployeeResponse toggleStatus(Long id);

    EmployeeResponse uploadProfileImage(Long id, MultipartFile file);

    String generateEmployeeCode();
}
