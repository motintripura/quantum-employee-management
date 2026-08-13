package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.AttendanceRequest;
import com.employee.employee_management.dto.AttendanceResponse;
import com.employee.employee_management.dto.CheckInRequest;
import com.employee.employee_management.dto.CheckOutRequest;
import com.employee.employee_management.entity.Attendance;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.ForbiddenException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.repository.AttendanceRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee employee;
    private User adminUser;

    @BeforeEach
    void setUp() {
        Department department = Department.builder().id(1L).name("IT").build();
        Designation designation = Designation.builder()
                .id(1L).name("Software Engineer").department(department).build();
        employee = Employee.builder()
                .id(1L).employeeCode("EMP0001").firstName("John").lastName("Doe")
                .email("john@ems.com").department(department).designation(designation).build();
        adminUser = User.builder().id(1L).username("admin").email("admin@ems.com")
                .role(Role.ADMIN).enabled(true).build();
    }

    private void setAuthentication(String username, Role role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "pw",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void record_computesWorkingHoursAndOvertime() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findByEmployeeIdAndAttendanceDate(1L, LocalDate.of(2026, 8, 1)))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceRequest request = AttendanceRequest.builder()
                .employeeId(1L)
                .attendanceDate(LocalDate.of(2026, 8, 1))
                .checkIn(LocalTime.of(9, 0))
                .checkOut(LocalTime.of(18, 0))
                .status(AttendanceStatus.PRESENT)
                .build();

        AttendanceResponse response = attendanceService.record(request);

        assertEquals(0, new BigDecimal("9.00").compareTo(response.getWorkingHours()));
        assertEquals(0, new BigDecimal("1.00").compareTo(response.getOvertime()));
        assertEquals(AttendanceStatus.PRESENT, response.getStatus());
    }

    @Test
    void record_checkOutBeforeCheckIn_throws() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        AttendanceRequest request = AttendanceRequest.builder()
                .employeeId(1L)
                .attendanceDate(LocalDate.of(2026, 8, 1))
                .checkIn(LocalTime.of(18, 0))
                .checkOut(LocalTime.of(9, 0))
                .status(AttendanceStatus.PRESENT)
                .build();

        assertThrows(BadRequestException.class, () -> attendanceService.record(request));
    }

    @Test
    void checkIn_employeeForbiddenForOtherEmployee() {
        setAuthentication("EMP0001", Role.EMPLOYEE);
        User empUser = User.builder().id(2L).username("EMP0001").role(Role.EMPLOYEE)
                .employee(employee).enabled(true).build();
        when(userRepository.findByUsername("EMP0001")).thenReturn(Optional.of(empUser));

        CheckInRequest request = CheckInRequest.builder().employeeId(99L).build();

        assertThrows(ForbiddenException.class, () -> attendanceService.checkIn(request));
    }

    @Test
    void checkIn_alreadyCheckedIn_throws() {
        setAuthentication("admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        Attendance existing = Attendance.builder()
                .employee(employee).attendanceDate(LocalDate.now())
                .checkIn(LocalTime.of(9, 0)).workingHours(BigDecimal.ZERO).build();
        when(attendanceRepository.findByEmployeeIdAndAttendanceDate(1L, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        CheckInRequest request = CheckInRequest.builder().employeeId(1L).build();

        assertThrows(BadRequestException.class, () -> attendanceService.checkIn(request));
    }

    @Test
    void checkOut_withoutCheckIn_throws() {
        setAuthentication("admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(attendanceRepository.findByEmployeeIdAndAttendanceDate(1L, LocalDate.now()))
                .thenReturn(Optional.empty());

        CheckOutRequest request = CheckOutRequest.builder().employeeId(1L).build();

        assertThrows(BadRequestException.class, () -> attendanceService.checkOut(request));
    }

    @Test
    void search_employeeScopedToOwnRecords() {
        setAuthentication("EMP0001", Role.EMPLOYEE);
        User empUser = User.builder().id(2L).username("EMP0001").role(Role.EMPLOYEE)
                .employee(employee).enabled(true).build();
        when(userRepository.findByUsername("EMP0001")).thenReturn(Optional.of(empUser));

        Attendance record = Attendance.builder()
                .id(1L).employee(employee).attendanceDate(LocalDate.now())
                .status(AttendanceStatus.PRESENT)
                .workingHours(new BigDecimal("8.00")).overtime(BigDecimal.ZERO).build();
        when(attendanceRepository.search(eq(1L), isNull(), isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(record)));

        var result = attendanceService.search(null, null, null, null, null, PageRequest.of(0, 10));

        assertEquals(1L, result.getTotalElements());
        verify(attendanceRepository).search(eq(1L), isNull(), isNull(), isNull(), isNull(), any());
    }

    @Test
    void search_employeeTryingOtherEmployee_throws() {
        setAuthentication("EMP0001", Role.EMPLOYEE);
        User empUser = User.builder().id(2L).username("EMP0001").role(Role.EMPLOYEE)
                .employee(employee).enabled(true).build();
        when(userRepository.findByUsername("EMP0001")).thenReturn(Optional.of(empUser));

        assertThrows(ForbiddenException.class,
                () -> attendanceService.search(99L, null, null, null, null, PageRequest.of(0, 10)));
    }

    @Test
    void monthlyReport_invalidMonth_throws() {
        assertThrows(BadRequestException.class, () -> attendanceService.monthlyReport(13, 2026));
    }

    @Test
    void monthlyReport_aggregatesByStatus() {
        setAuthentication("admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        LocalDate d1 = LocalDate.of(2026, 8, 1);
        LocalDate d2 = LocalDate.of(2026, 8, 2);
        Attendance present = Attendance.builder().id(1L).employee(employee)
                .attendanceDate(d1).status(AttendanceStatus.PRESENT)
                .workingHours(new BigDecimal("8.00")).overtime(BigDecimal.ZERO).build();
        Attendance late = Attendance.builder().id(2L).employee(employee)
                .attendanceDate(d2).status(AttendanceStatus.LATE)
                .workingHours(new BigDecimal("6.00")).overtime(BigDecimal.ZERO).build();
        when(attendanceRepository.findByAttendanceDateBetween(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(present, late));

        var result = attendanceService.monthlyReport(8, 2026);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPresentDays());
        assertEquals(1, result.get(0).getLateDays());
        assertEquals(2, result.get(0).getTotalDays());
        assertEquals(0, new BigDecimal("14.00").compareTo(result.get(0).getTotalWorkingHours()));
    }

    @Test
    void getEmployeeAttendance_unknownEmployee_throws() {
        setAuthentication("admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.getEmployeeAttendance(1L, null, null));
    }
}