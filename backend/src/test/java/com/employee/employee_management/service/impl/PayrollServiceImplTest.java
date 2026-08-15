package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.PayrollRequest;
import com.employee.employee_management.dto.PayrollResponse;
import com.employee.employee_management.entity.*;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.PayrollRepository;
import com.employee.employee_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceImplTest {

    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PayrollServiceImpl payrollService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        Department department = Department.builder().id(1L).name("IT").build();
        Designation designation = Designation.builder()
                .id(1L).name("Software Engineer").department(department).build();
        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP0001")
                .firstName("John")
                .lastName("Doe")
                .email("john@ems.com")
                .department(department)
                .designation(designation)
                .salary(new BigDecimal("60000"))
                .build();
    }

    @Test
    void create_computesGrossAndNetSalary() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonthAndYear(1L, 8, 2026)).thenReturn(false);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollRequest request = PayrollRequest.builder()
                .employeeId(1L)
                .month(8)
                .year(2026)
                .basicSalary(new BigDecimal("60000"))
                .houseAllowance(new BigDecimal("10000"))
                .transportAllowance(new BigDecimal("5000"))
                .medicalAllowance(new BigDecimal("3000"))
                .bonus(new BigDecimal("2000"))
                .tax(new BigDecimal("8000"))
                .deduction(new BigDecimal("1000"))
                .build();

        PayrollResponse response = payrollService.create(request);

        assertEquals(0, new BigDecimal("80000").compareTo(response.getGrossSalary()));
        assertEquals(0, new BigDecimal("71000").compareTo(response.getNetSalary()));
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
    }

    @Test
    void create_defaultsBasicSalaryToEmployeeSalary() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonthAndYear(1L, 5, 2026)).thenReturn(false);
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollRequest request = PayrollRequest.builder()
                .employeeId(1L)
                .month(5)
                .year(2026)
                .build();

        PayrollResponse response = payrollService.create(request);

        assertEquals(0, new BigDecimal("60000").compareTo(response.getGrossSalary()));
    }

    @Test
    void create_duplicate_throwsDuplicate() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(payrollRepository.existsByEmployeeIdAndMonthAndYear(1L, 8, 2026)).thenReturn(true);

        PayrollRequest request = PayrollRequest.builder()
                .employeeId(1L).month(8).year(2026).build();

        assertThrows(DuplicateResourceException.class, () -> payrollService.create(request));
        verify(payrollRepository, never()).save(any(Payroll.class));
    }

    @Test
    void create_employeeNotFound_throwsNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        PayrollRequest request = PayrollRequest.builder()
                .employeeId(1L).month(8).year(2026).build();

        assertThrows(ResourceNotFoundException.class, () -> payrollService.create(request));
    }

    @Test
    void markAsPaid_setsStatusAndPaymentDate() {
        Payroll payroll = Payroll.builder()
                .id(1L)
                .employee(employee)
                .month(8).year(2026)
                .paymentStatus(PaymentStatus.PENDING).build();
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollResponse response = payrollService.markAsPaid(1L);

        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        assertNotNull(response.getPaymentDate());
        assertTrue(!response.getPaymentDate().isAfter(LocalDate.now()));
    }

    @Test
    void markAsPaid_preservesExistingPaymentDate() {
        Payroll payroll = Payroll.builder()
                .id(1L)
                .employee(employee)
                .month(8).year(2026)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDate.of(2026, 8, 5))
                .build();
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));
        when(payrollRepository.save(any(Payroll.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PayrollResponse response = payrollService.markAsPaid(1L);

        assertEquals(LocalDate.of(2026, 8, 5), response.getPaymentDate());
    }

    @Test
    void getById_employeeAccessToOthers_throwsForbidden() {
        User other = User.builder()
                .id(2L).username("OTHER").email("other@e.com")
                .role(Role.EMPLOYEE).enabled(true).build();
        Employee otherEmployee = Employee.builder()
                .id(9L).employeeCode("EMP0009").firstName("Other").lastName("Guy").email("og@e.com")
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .build();
        other.setEmployee(otherEmployee);

        Payroll payroll = Payroll.builder()
                .id(1L).employee(employee).month(8).year(2026)
                .paymentStatus(PaymentStatus.PENDING).build();
        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));
        when(userRepository.findByUsername("OTHER")).thenReturn(Optional.of(other));

        setAuthentication("OTHER");

        assertThrows(com.employee.employee_management.exception.ForbiddenException.class,
                () -> payrollService.getById(1L));
        clearAuthentication();
    }

    private void setAuthentication(String username) {
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        username, "pw", java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_EMPLOYEE"))));
    }

    private void clearAuthentication() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}