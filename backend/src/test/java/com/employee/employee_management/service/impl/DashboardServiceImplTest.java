package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.CategoryAmount;
import com.employee.employee_management.dto.CategoryCount;
import com.employee.employee_management.dto.DashboardStatisticsResponse;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.entity.LeaveRequestStatus;
import com.employee.employee_management.repository.AttendanceRepository;
import com.employee.employee_management.repository.DepartmentRepository;
import com.employee.employee_management.repository.DesignationRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.LeaveRequestRepository;
import com.employee.employee_management.repository.LeaveTypeRepository;
import com.employee.employee_management.repository.PayrollRepository;
import com.employee.employee_management.repository.PerformanceReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private DesignationRepository designationRepository;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    @Mock
    private LeaveTypeRepository leaveTypeRepository;
    @Mock
    private PayrollRepository payrollRepository;
    @Mock
    private PerformanceReviewRepository performanceReviewRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getStatistics_aggregatesCounts() {
        when(employeeRepository.count()).thenReturn(10L);
        when(employeeRepository.countByStatusTrue()).thenReturn(8L);
        when(employeeRepository.countByStatusFalse()).thenReturn(2L);
        when(departmentRepository.count()).thenReturn(5L);
        when(designationRepository.count()).thenReturn(12L);
        when(leaveTypeRepository.countByStatusTrue()).thenReturn(6L);
        when(leaveRequestRepository.countByStatus(LeaveRequestStatus.PENDING)).thenReturn(3L);

        LocalDate today = LocalDate.now();
        when(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.PRESENT)).thenReturn(6L);
        when(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.ABSENT)).thenReturn(1L);
        when(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.LATE)).thenReturn(2L);
        when(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.HALF_DAY)).thenReturn(0L);
        when(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.LEAVE)).thenReturn(1L);
        when(payrollRepository.sumNetSalaryByMonthAndYear(today.getMonthValue(), today.getYear()))
                .thenReturn(new BigDecimal("500000"));

        DashboardStatisticsResponse stats = dashboardService.getStatistics();

        assertEquals(10L, stats.getTotalEmployees());
        assertEquals(8L, stats.getActiveEmployees());
        assertEquals(2L, stats.getInactiveEmployees());
        assertEquals(5L, stats.getTotalDepartments());
        assertEquals(6L, stats.getTodayPresent());
        assertEquals(2L, stats.getTodayLate());
        assertEquals(3L, stats.getPendingLeaveRequests());
        assertEquals(0, new BigDecimal("500000").compareTo(stats.getMonthlyPayrollTotal()));
    }

    @Test
    void getDepartmentDistribution_mapsRows() {
        when(employeeRepository.countByDepartment())
                .thenReturn(List.<Object[]>of(new Object[]{"IT", 4L}, new Object[]{"HR", 3L}));

        List<CategoryCount> result = dashboardService.getDepartmentDistribution();

        assertEquals(2, result.size());
        assertEquals("IT", result.get(0).getName());
        assertEquals(4L, result.get(0).getCount());
    }

    @Test
    void getPayrollOverview_fillsAllTwelveMonths() {
        when(payrollRepository.sumGrossSalaryByMonth(2026))
                .thenReturn(List.<Object[]>of(new Object[]{8, new BigDecimal("130000")}));

        List<CategoryAmount> result = dashboardService.getPayrollOverview(2026);

        assertEquals(12, result.size());
        assertEquals(0, new BigDecimal("130000").compareTo(result.get(7).getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.get(0).getAmount()));
        assertEquals("JANUARY", result.get(0).getName());
        assertEquals("AUGUST", result.get(7).getName());
    }

    @Test
    void getPayrollOverview_nullYearDefaultsCurrent() {
        when(payrollRepository.sumGrossSalaryByMonth(LocalDate.now().getYear())).thenReturn(List.of());

        List<CategoryAmount> result = dashboardService.getPayrollOverview(null);

        assertEquals(12, result.size());
    }

    @Test
    void getEmployeeStatusDistribution_returnsActiveAndInactive() {
        when(employeeRepository.countByStatusTrue()).thenReturn(7L);
        when(employeeRepository.countByStatusFalse()).thenReturn(3L);

        List<CategoryCount> result = dashboardService.getEmployeeStatusDistribution();

        assertEquals(2, result.size());
        assertEquals("ACTIVE", result.get(0).getName());
        assertEquals(7L, result.get(0).getCount());
        assertEquals("INACTIVE", result.get(1).getName());
    }

    @Test
    void getPerformanceOverview_mapsRatingRows() {
        when(performanceReviewRepository.countByRating())
                .thenReturn(List.<Object[]>of(new Object[]{4, 2L}));

        List<CategoryCount> result = dashboardService.getPerformanceOverview();

        assertEquals("rating_4", result.get(0).getName());
        assertEquals(2L, result.get(0).getCount());
    }
}