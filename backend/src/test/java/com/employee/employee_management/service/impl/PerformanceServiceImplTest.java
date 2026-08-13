package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.PerformanceRequest;
import com.employee.employee_management.dto.PerformanceResponse;
import com.employee.employee_management.dto.StatusCountResponse;
import com.employee.employee_management.entity.Department;
import com.employee.employee_management.entity.Designation;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.PerformanceReview;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.ForbiddenException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.PerformanceReviewRepository;
import com.employee.employee_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceImplTest {

    @Mock
    private PerformanceReviewRepository performanceReviewRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PerformanceServiceImpl performanceService;

    private Employee employee;
    private User adminUser;
    private User employeeUser;

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
        employeeUser = User.builder().id(2L).username("EMP0001").role(Role.EMPLOYEE)
                .employee(employee).enabled(true).build();
    }

    private void setAuthentication(String username, Role role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, "pw",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private PerformanceRequest validRequest() {
        return PerformanceRequest.builder()
                .employeeId(1L)
                .reviewDate(LocalDate.of(2026, 8, 1))
                .rating(4)
                .strengths("Good")
                .build();
    }

    @Test
    void create_reviewerDefaultsToCurrentUser() {
        setAuthentication("admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(performanceReviewRepository.save(any(PerformanceReview.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PerformanceResponse response = performanceService.create(validRequest());

        assertEquals(4, response.getRating());
        assertEquals("admin", response.getReviewerUsername());
    }

    @Test
    void create_explicitReviewerId() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        User manager = User.builder().id(3L).username("manager1").role(Role.MANAGER).enabled(true).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(manager));
        when(performanceReviewRepository.save(any(PerformanceReview.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PerformanceRequest request = validRequest();
        request.setReviewerId(3L);

        PerformanceResponse response = performanceService.create(request);

        assertEquals("manager1", response.getReviewerUsername());
    }

    @Test
    void create_reviewerNotFound_throws() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        PerformanceRequest request = validRequest();
        request.setReviewerId(99L);

        assertThrows(ResourceNotFoundException.class, () -> performanceService.create(request));
    }

    @Test
    void create_employeeNotFound_throws() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> performanceService.create(validRequest()));
    }

    @Test
    void getById_employeeViewingOtherReview_throws() {
        setAuthentication("EMP0001", Role.EMPLOYEE);
        when(userRepository.findByUsername("EMP0001")).thenReturn(Optional.of(employeeUser));

        Employee other = Employee.builder().id(9L).employeeCode("EMP0009").build();
        PerformanceReview review = PerformanceReview.builder().id(1L).employee(other).build();
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(review));

        assertThrows(ForbiddenException.class, () -> performanceService.getById(1L));
    }

    @Test
    void getById_employeeOwnReview_ok() {
        setAuthentication("EMP0001", Role.EMPLOYEE);
        when(userRepository.findByUsername("EMP0001")).thenReturn(Optional.of(employeeUser));

        PerformanceReview review = PerformanceReview.builder()
                .id(1L).employee(employee).reviewer(adminUser)
                .reviewDate(LocalDate.of(2026, 8, 1)).rating(5).build();
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(review));

        PerformanceResponse response = performanceService.getById(1L);

        assertEquals(5, response.getRating());
        assertEquals("John Doe", response.getEmployeeName());
    }

    @Test
    void getEmployeePerformance_employeeOther_throws() {
        setAuthentication("EMP0001", Role.EMPLOYEE);
        when(userRepository.findByUsername("EMP0001")).thenReturn(Optional.of(employeeUser));

        assertThrows(ForbiddenException.class,
                () -> performanceService.getEmployeePerformance(9L));
    }

    @Test
    void delete_removesReview() {
        PerformanceReview review = PerformanceReview.builder().id(1L).employee(employee).build();
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(review));

        performanceService.delete(1L);

        verify(performanceReviewRepository).delete(review);
    }

    @Test
    void ratingReport_mapsRows() {
        when(performanceReviewRepository.countByRating())
                .thenReturn(List.of(new Object[]{4, 2L}, new Object[]{5, 1L}));

        List<StatusCountResponse> report = performanceService.ratingReport();

        assertEquals(2, report.size());
        assertEquals("rating_4", report.get(0).getName());
        assertEquals(2, report.get(0).getCount());
    }

    @Test
    void update_updatesReview() {
        setAuthentication("admin", Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        PerformanceReview review = PerformanceReview.builder()
                .id(1L).employee(employee).reviewer(adminUser)
                .reviewDate(LocalDate.of(2026, 8, 1)).rating(3).build();
        when(performanceReviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(performanceReviewRepository.save(any(PerformanceReview.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PerformanceRequest request = validRequest();
        request.setRating(5);
        request.setComments("Improved");

        PerformanceResponse response = performanceService.update(1L, request);

        assertEquals(5, response.getRating());
        assertEquals("Improved", response.getComments());
    }
}