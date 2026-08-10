package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.DesignationRequest;
import com.employee.employee_management.dto.DesignationResponse;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.mapper.DesignationMapper;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.service.DesignationService;
import com.employee.employee_management.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DesignationResponse> getAll(String keyword, Long departmentId, Boolean status, Pageable pageable) {
        Page<Designation> page = designationRepository.search(normalize(keyword), departmentId, status, pageable);
        return PageResponse.from(page.map(DesignationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DesignationResponse> getActiveList(Long departmentId) {
        List<Designation> designations = departmentId == null
                ? designationRepository.findByStatusTrueOrderByNameAsc()
                : designationRepository.findByDepartmentIdAndStatusTrueOrderByNameAsc(departmentId);
        return designations.stream().map(DesignationMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse getById(Long id) {
        return DesignationMapper.toResponse(findDesignation(id));
    }

    @Override
    @Transactional
    public DesignationResponse create(DesignationRequest request) {
        Department department = findDepartment(request.getDepartmentId());
        String name = request.getName().trim();
        if (designationRepository.existsByNameIgnoreCaseAndDepartmentId(name, department.getId())) {
            throw new DuplicateResourceException("Designation already exists in this department: " + name);
        }
        Designation designation = Designation.builder()
                .name(name)
                .description(request.getDescription())
                .department(department)
                .status(request.getStatus() == null ? Boolean.TRUE : request.getStatus())
                .build();
        designationRepository.save(designation);
        return DesignationMapper.toResponse(designation);
    }

    @Override
    @Transactional
    public DesignationResponse update(Long id, DesignationRequest request) {
        Designation designation = findDesignation(id);
        Department department = findDepartment(request.getDepartmentId());
        String name = request.getName().trim();
        if (designationRepository.existsByNameIgnoreCaseAndDepartmentIdAndIdNot(name, department.getId(), id)) {
            throw new DuplicateResourceException("Designation already exists in this department: " + name);
        }
        designation.setName(name);
        designation.setDescription(request.getDescription());
        designation.setDepartment(department);
        if (request.getStatus() != null) {
            designation.setStatus(request.getStatus());
        }
        designationRepository.save(designation);
        return DesignationMapper.toResponse(designation);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Designation designation = findDesignation(id);
        long employeeCount = employeeRepository.countByDesignationId(id);
        if (employeeCount > 0) {
            throw new BadRequestException("Cannot delete designation '" + designation.getName()
                    + "' because " + employeeCount + " employee(s) are assigned to it");
        }
        designationRepository.delete(designation);
    }

    @Override
    @Transactional
    public DesignationResponse toggleStatus(Long id) {
        Designation designation = findDesignation(id);
        designation.setStatus(!Boolean.TRUE.equals(designation.getStatus()));
        designationRepository.save(designation);
        return DesignationMapper.toResponse(designation);
    }

    private Designation findDesignation(Long id) {
        return designationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found with id: " + id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private String normalize(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}
