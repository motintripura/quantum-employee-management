package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.DepartmentRequest;
import com.employee.employee_management.dto.DepartmentResponse;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private DesignationRepository designationRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder().id(1L).name("IT").description("IT dept").status(true).build();
    }

    @Test
    void create_duplicateName_throws() {
        when(departmentRepository.existsByNameIgnoreCase("IT")).thenReturn(true);

        DepartmentRequest request = DepartmentRequest.builder().name("IT").build();

        assertThrows(DuplicateResourceException.class, () -> departmentService.create(request));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    void create_valid_savesDepartment() {
        when(departmentRepository.existsByNameIgnoreCase("IT")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentRequest request = DepartmentRequest.builder().name("IT").description("IT dept").build();

        DepartmentResponse response = departmentService.create(request);

        assertEquals("IT", response.getName());
        assertEquals("IT dept", response.getDescription());
        assertTrue(response.getStatus());
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void update_notFound_throws() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        DepartmentRequest request = DepartmentRequest.builder().name("Ops").build();

        assertThrows(ResourceNotFoundException.class, () -> departmentService.update(99L, request));
    }

    @Test
    void update_duplicateNameExcludingSelf_throws() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameIgnoreCaseAndIdNot("Ops", 1L)).thenReturn(true);

        DepartmentRequest request = DepartmentRequest.builder().name("Ops").build();

        assertThrows(DuplicateResourceException.class, () -> departmentService.update(1L, request));
    }

    @Test
    void update_valid_updatesFields() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.existsByNameIgnoreCaseAndIdNot("IT Support", 1L)).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentRequest request = DepartmentRequest.builder()
                .name("IT Support").description("Updated").status(false).build();

        DepartmentResponse response = departmentService.update(1L, request);

        assertEquals("IT Support", response.getName());
        assertFalse(response.getStatus());
    }

    @Test
    void delete_withEmployees_throws() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(3L);

        assertThrows(BadRequestException.class, () -> departmentService.delete(1L));
        verify(departmentRepository, never()).delete(any(Department.class));
    }

    @Test
    void delete_withDesignations_throws() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);
        when(designationRepository.existsByDepartmentId(1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> departmentService.delete(1L));
        verify(departmentRepository, never()).delete(any(Department.class));
    }

    @Test
    void delete_ok_removesDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(0L);
        when(designationRepository.existsByDepartmentId(1L)).thenReturn(false);

        departmentService.delete(1L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void toggleStatus_flips() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentResponse response = departmentService.toggleStatus(1L);

        assertFalse(response.getStatus());
    }

    @Test
    void getAll_searchReturnsMappedPage() {
        when(departmentRepository.search(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(department)));
        when(employeeRepository.countByDepartmentId(1L)).thenReturn(5L);

        var result = departmentService.getAll("it", true, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getEmployeeCount());
    }
}