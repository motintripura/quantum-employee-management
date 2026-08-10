package com.employee.employee_management.service;

import com.employee.employee_management.dto.DesignationRequest;
import com.employee.employee_management.dto.DesignationResponse;
import com.employee.employee_management.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DesignationService {

    PageResponse<DesignationResponse> getAll(String keyword, Long departmentId, Boolean status, Pageable pageable);

    List<DesignationResponse> getActiveList(Long departmentId);

    DesignationResponse getById(Long id);

    DesignationResponse create(DesignationRequest request);

    DesignationResponse update(Long id, DesignationRequest request);

    void delete(Long id);

    DesignationResponse toggleStatus(Long id);
}
