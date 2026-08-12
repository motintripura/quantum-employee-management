package com.employee.employee_management.controller;

import com.employee.employee_management.dto.CategoryAmount;
import com.employee.employee_management.dto.CategoryCount;
import com.employee.employee_management.dto.DashboardStatisticsResponse;
import com.employee.employee_management.dto.RecentActivityResponse;
import com.employee.employee_management.service.DashboardService;
import com.employee.employee_management.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DashboardStatisticsResponse>> statistics() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStatistics()));
    }

    @GetMapping("/department")
    public ResponseEntity<ApiResponse<List<CategoryCount>>> departmentDistribution() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDepartmentDistribution()));
    }

    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<CategoryCount>>> attendanceOverview() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getAttendanceOverview()));
    }

    @GetMapping("/leave")
    public ResponseEntity<ApiResponse<List<CategoryCount>>> leaveOverview() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getLeaveOverview()));
    }

    @GetMapping("/payroll")
    public ResponseEntity<ApiResponse<List<CategoryAmount>>> payrollOverview(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getPayrollOverview(year)));
    }

    @GetMapping("/performance")
    public ResponseEntity<ApiResponse<List<CategoryCount>>> performanceOverview() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getPerformanceOverview()));
    }

    @GetMapping("/employee-status")
    public ResponseEntity<ApiResponse<List<CategoryCount>>> employeeStatus() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getEmployeeStatusDistribution()));
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<List<RecentActivityResponse>>> recentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getRecentActivities(limit)));
    }
}
