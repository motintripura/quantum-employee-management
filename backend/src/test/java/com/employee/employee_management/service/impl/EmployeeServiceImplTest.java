package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.EmployeeRequest;
import com.employee.employee_management.dto.EmployeeResponse;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private DesignationRepository designationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Department department;
    private Designation designation;
    private Employee employee;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(employeeService, "uploadDir", "./target/test-uploads");
        department = Department.builder().id(1L).name("IT").status(true).build();
        designation = Designation.builder().id(1L).name("Software Engineer")
                .department(department).status(true).build();
        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP0001")
                .firstName("John")
                .lastName("Doe")
                .email("john@ems.com")
                .department(department)
                .designation(designation)
                .salary(new BigDecimal("60000"))
                .status(true)
                .build();
    }

    private EmployeeRequest validRequest() {
        return EmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@ems.com")
                .joiningDate(LocalDate.of(2024, 1, 15))
                .departmentId(1L)
                .designationId(1L)
                .salary(new BigDecimal("60000"))
                .status(true)
                .build();
    }

    @Test
    void getById_found_returnsResponse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponse response = employeeService.getById(1L);

        assertEquals("EMP0001", response.getEmployeeCode());
        assertEquals("John Doe", response.getFullName());
        assertEquals("IT", response.getDepartmentName());
    }

    @Test
    void getById_notFound_throws() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> employeeService.getById(99L));
    }

    @Test
    void create_duplicateEmail_throws() {
        when(employeeRepository.existsByEmailIgnoreCase("john@ems.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> employeeService.create(validRequest()));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void create_duplicateCode_throws() {
        when(employeeRepository.existsByEmailIgnoreCase("john@ems.com")).thenReturn(false);
        when(employeeRepository.existsByEmployeeCodeIgnoreCase("EMP0001")).thenReturn(true);

        EmployeeRequest request = validRequest();
        request.setEmployeeCode("EMP0001");

        assertThrows(DuplicateResourceException.class, () -> employeeService.create(request));
    }

    @Test
    void create_designationNotInDepartment_throws() {
        when(employeeRepository.existsByEmailIgnoreCase("john@ems.com")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));

        Department other = Department.builder().id(2L).name("HR").build();
        Designation otherDesignation = Designation.builder().id(1L).name("Software Engineer")
                .department(other).build();
        when(designationRepository.findById(1L)).thenReturn(Optional.of(otherDesignation));

        assertThrows(BadRequestException.class, () -> employeeService.create(validRequest()));
    }

    @Test
    void create_valid_createsEmployeeAndUser() {
        when(employeeRepository.existsByEmailIgnoreCase("john@ems.com")).thenReturn(false);
        when(employeeRepository.findCodesByPrefix("EMP")).thenReturn(List.of("EMP0001"));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(passwordEncoder.encode(Constants.DEFAULT_USER_PASSWORD)).thenReturn("encoded");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.create(validRequest());

        assertEquals("EMP0002", response.getEmployeeCode());
        assertEquals(Constants.DEFAULT_USER_PASSWORD, response.getTemporaryPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_duplicateEmailExcludingSelf_throws() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("other@ems.com", 1L)).thenReturn(true);

        EmployeeRequest request = validRequest();
        request.setEmail("other@ems.com");

        assertThrows(DuplicateResourceException.class, () -> employeeService.update(1L, request));
    }

    @Test
    void update_valid_updatesEmployeeAndUserEmail() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(1L))).thenReturn(false);
        when(employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(anyString(), eq(1L))).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(designationRepository.findById(1L)).thenReturn(Optional.of(designation));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        User linkedUser = User.builder().id(1L).username("EMP0001").email("john@ems.com").build();
        when(userRepository.findByEmployeeId(1L)).thenReturn(Optional.of(linkedUser));

        EmployeeRequest request = validRequest();
        request.setEmail("john.new@ems.com");
        request.setSalary(new BigDecimal("70000"));

        EmployeeResponse response = employeeService.update(1L, request);

        assertEquals("john.new@ems.com", response.getEmail());
        assertEquals(0, new BigDecimal("70000").compareTo(response.getSalary()));
        assertEquals("john.new@ems.com", linkedUser.getEmail());
        verify(userRepository).save(linkedUser);
    }

    @Test
    void delete_removesLinkedUserAndEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        User linkedUser = User.builder().id(1L).username("EMP0001").build();
        when(userRepository.findByEmployeeId(1L)).thenReturn(Optional.of(linkedUser));

        employeeService.delete(1L);

        verify(userRepository).delete(linkedUser);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void toggleStatus_flipsStatus() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        EmployeeResponse response = employeeService.toggleStatus(1L);

        assertFalse(response.getStatus());
    }

    @Test
    void generateEmployeeCode_returnsNextSequential() {
        when(employeeRepository.findCodesByPrefix("EMP"))
                .thenReturn(List.of("EMP0001", "EMP0007", "EMP0012"));

        assertEquals("EMP0013", employeeService.generateEmployeeCode());
    }

    @Test
    void generateEmployeeCode_noCodes_returnsFirst() {
        when(employeeRepository.findCodesByPrefix("EMP")).thenReturn(List.of());

        assertEquals("EMP0001", employeeService.generateEmployeeCode());
    }

    @Test
    void uploadProfileImage_rejectsNonImage() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        org.springframework.web.multipart.MultipartFile file =
                mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(BadRequestException.class, () -> employeeService.uploadProfileImage(1L, file));
    }

    @Test
    void getAll_searchDelegatesToRepository() {
        org.springframework.data.domain.Page<Employee> page =
                new org.springframework.data.domain.PageImpl<>(List.of(employee));
        when(employeeRepository.search(any(), any(), any(), any(), any()))
                .thenReturn(page);

        var result = employeeService.getAll("john", 1L, 1L, true,
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getFullName());
    }
}