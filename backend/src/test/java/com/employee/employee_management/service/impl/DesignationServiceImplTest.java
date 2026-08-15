package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.DesignationRequest;
import com.employee.employee_management.dto.DesignationResponse;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
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
class DesignationServiceImplTest {

    @Mock
    private DesignationRepository designationRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DesignationServiceImpl designationService;

    private Department department;
    private Designation designation;

    @BeforeEach
    void setUp() {
        department = Department.builder().id(1L).name("IT").build();
        designation = Designation.builder().id(1L).name("Software Engineer")
                .description("SWE").department(department).status(true).build();
    }

    @Test
    void create_departmentNotFound_throws() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        DesignationRequest request = DesignationRequest.builder()
                .name("Developer").departmentId(99L).build();

        assertThrows(ResourceNotFoundException.class, () -> designationService.create(request));
    }

    @Test
    void create_duplicateInDepartment_throws() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.existsByNameIgnoreCaseAndDepartmentId("Software Engineer", 1L))
                .thenReturn(true);

        DesignationRequest request = DesignationRequest.builder()
                .name("Software Engineer").departmentId(1L).build();

        assertThrows(DuplicateResourceException.class, () -> designationService.create(request));
        verify(designationRepository, never()).save(any(Designation.class));
    }

    @Test
    void create_valid_saves() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.existsByNameIgnoreCaseAndDepartmentId("QA Engineer", 1L))
                .thenReturn(false);
        when(designationRepository.save(any(Designation.class))).thenAnswer(inv -> inv.getArgument(0));

        DesignationRequest request = DesignationRequest.builder()
                .name("QA Engineer").departmentId(1L).build();

        DesignationResponse response = designationService.create(request);

        assertEquals("QA Engineer", response.getName());
        assertEquals("IT", response.getDepartmentName());
    }

    @Test
    void update_movesToAnotherDepartment_checksUniquenessThere() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(departmentRepository.findById(2L))
                .thenReturn(Optional.of(Department.builder().id(2L).name("HR").build()));
        when(designationRepository.existsByNameIgnoreCaseAndDepartmentIdAndIdNot("Software Engineer", 2L, 1L))
                .thenReturn(false);
        when(designationRepository.save(any(Designation.class))).thenAnswer(inv -> inv.getArgument(0));

        DesignationRequest request = DesignationRequest.builder()
                .name("Software Engineer").departmentId(2L).build();

        DesignationResponse response = designationService.update(1L, request);

        assertEquals("HR", response.getDepartmentName());
    }

    @Test
    void delete_withEmployees_throws() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(employeeRepository.countByDesignationId(1L)).thenReturn(2L);

        assertThrows(BadRequestException.class, () -> designationService.delete(1L));
        verify(designationRepository, never()).delete(any(Designation.class));
    }

    @Test
    void delete_ok_removes() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(employeeRepository.countByDesignationId(1L)).thenReturn(0L);

        designationService.delete(1L);

        verify(designationRepository).delete(designation);
    }

    @Test
    void toggleStatus_flips() {
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(designationRepository.save(any(Designation.class))).thenAnswer(inv -> inv.getArgument(0));

        DesignationResponse response = designationService.toggleStatus(1L);

        assertFalse(response.getStatus());
    }

    @Test
    void getAll_searchReturnsMappedPage() {
        when(designationRepository.search(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(designation)));

        var result = designationService.getAll("soft", 1L, true, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Software Engineer", result.getContent().get(0).getName());
    }

    @Test
    void getActiveList_filtersByDepartment() {
        when(designationRepository.findByDepartmentIdAndStatusTrueOrderByNameAsc(1L))
                .thenReturn(List.of(designation));

        var result = designationService.getActiveList(1L);

        assertEquals(1, result.size());
        assertEquals("Software Engineer", result.get(0).getName());
    }
}