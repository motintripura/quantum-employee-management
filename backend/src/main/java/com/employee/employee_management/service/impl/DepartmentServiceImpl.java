package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.DepartmentRequest;
import com.employee.employee_management.dto.DepartmentResponse;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.mapper.DepartmentMapper;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.service.DepartmentService;
import com.employee.employee_management.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> getAll(String keyword, Boolean status, Pageable pageable) {
        Page<Department> page = departmentRepository.search(normalize(keyword), status, pageable);
        return PageResponse.from(page.map(d -> DepartmentMapper.toResponse(d, employeeCount(d.getId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getActiveList() {
        return departmentRepository.findAllByOrderByNameAsc().stream()
                .filter(Department::getStatus)
                .map(d -> DepartmentMapper.toResponse(d, employeeCount(d.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = findDepartment(id);
        return DepartmentMapper.toResponse(department, employeeCount(id));
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String name = request.getName().trim();
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Department already exists: " + name);
        }
        Department department = Department.builder()
                .name(name)
                .description(request.getDescription())
                .status(request.getStatus() == null ? Boolean.TRUE : request.getStatus())
                .build();
        departmentRepository.save(department);
        return DepartmentMapper.toResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = findDepartment(id);
        String name = request.getName().trim();
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Department already exists: " + name);
        }
        department.setName(name);
        department.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            department.setStatus(request.getStatus());
        }
        departmentRepository.save(department);
        return DepartmentMapper.toResponse(department, employeeCount(id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = findDepartment(id);
        long employeeCount = employeeRepository.countByDepartmentId(id);
        if (employeeCount > 0) {
            throw new BadRequestException("Cannot delete department '" + department.getName()
                    + "' because " + employeeCount + " employee(s) are assigned to it");
        }
        if (designationRepository.existsByDepartmentId(id)) {
            throw new BadRequestException("Cannot delete department '" + department.getName()
                    + "' because designations are assigned to it");
        }
        departmentRepository.delete(department);
    }

    @Override
    @Transactional
    public DepartmentResponse toggleStatus(Long id) {
        Department department = findDepartment(id);
        department.setStatus(!Boolean.TRUE.equals(department.getStatus()));
        departmentRepository.save(department);
        return DepartmentMapper.toResponse(department, employeeCount(id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private long employeeCount(Long departmentId) {
        return employeeRepository.countByDepartmentId(departmentId);
    }

    private String normalize(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.trim();
    }
}
