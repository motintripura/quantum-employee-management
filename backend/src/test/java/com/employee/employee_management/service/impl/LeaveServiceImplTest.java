package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.LeaveApplyRequest;
import com.employee.employee_management.dto.LeaveResponse;
import com.employee.employee_management.entity.*;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.ForbiddenException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private LeaveTypeRepository leaveTypeRepository;
    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee employee;
    private LeaveType leaveType;
    private User adminUser;

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
                .build();
        leaveType = LeaveType.builder().id(1L).name("CASUAL").defaultDays(10).status(true).build();
        adminUser = User.builder()
                .id(1L).username("admin").email("admin@ems.com")
                .role(Role.ADMIN).enabled(true).build();
    }

    private void setAuthentication(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "pw",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void apply_startDateAfterEndDate_throwsBadRequest() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        setAuthentication("admin");

        LocalDate end = LocalDate.now().plusDays(5);
        LeaveApplyRequest request = LeaveApplyRequest.builder()
                .employeeId(1L)
                .leaveTypeId(1L)
                .startDate(end)
                .endDate(end.minusDays(2))
                .build();

        assertThrows(BadRequestException.class, () -> leaveService.apply(request));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void apply_overlappingLeave_throwsBadRequest() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        setAuthentication("admin");

        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = start.plusDays(2);
        when(leaveRequestRepository.findOverlapping(1L, start, end))
                .thenReturn(List.of(new LeaveRequest()));

        LeaveApplyRequest request = LeaveApplyRequest.builder()
                .employeeId(1L).leaveTypeId(1L).startDate(start).endDate(end).build();

        assertThrows(BadRequestException.class, () -> leaveService.apply(request));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void apply_insufficientBalance_throwsBadRequest() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        setAuthentication("admin");

        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = start.plusDays(2);
        when(leaveRequestRepository.findOverlapping(1L, start, end)).thenReturn(List.of());
        when(leaveBalanceRepository.existsByEmployeeIdAndYear(1L, start.getYear())).thenReturn(true);
        LeaveBalance balance = LeaveBalance.builder()
                .id(1L)
                .employee(employee)
                .leaveType(leaveType)
                .year(start.getYear())
                .allocatedDays(new BigDecimal("2"))
                .usedDays(BigDecimal.ZERO)
                .remainingDays(new BigDecimal("2"))
                .build();
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, start.getYear()))
                .thenReturn(Optional.of(balance));

        LeaveApplyRequest request = LeaveApplyRequest.builder()
                .employeeId(1L).leaveTypeId(1L).startDate(start).endDate(end).build();

        assertThrows(BadRequestException.class, () -> leaveService.apply(request));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void apply_validRequest_savesPending() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveTypeRepository.findById(1L)).thenReturn(Optional.of(leaveType));
        setAuthentication("admin");

        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = start.plusDays(2);
        when(leaveRequestRepository.findOverlapping(1L, start, end)).thenReturn(List.of());
        when(leaveBalanceRepository.existsByEmployeeIdAndYear(1L, start.getYear())).thenReturn(true);
        LeaveBalance balance = LeaveBalance.builder()
                .id(1L)
                .employee(employee)
                .leaveType(leaveType)
                .year(start.getYear())
                .allocatedDays(new BigDecimal("10"))
                .usedDays(BigDecimal.ZERO)
                .remainingDays(new BigDecimal("10"))
                .build();
        when(leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(1L, 1L, start.getYear()))
                .thenReturn(Optional.of(balance));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        LeaveApplyRequest request = LeaveApplyRequest.builder()
                .employeeId(1L).leaveTypeId(1L).startDate(start).endDate(end).reason("vacation").build();

        LeaveResponse response = leaveService.apply(request);

        assertEquals(LeaveRequestStatus.PENDING, response.getStatus());
        assertEquals("EMP0001", response.getEmployeeCode());
        assertEquals(0, new BigDecimal("3").compareTo(response.getTotalDays()));
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }
}