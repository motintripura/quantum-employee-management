package com.employee.employee_management.service;

import com.employee.employee_management.dto.CategoryAmount;
import com.employee.employee_management.dto.CategoryCount;
import com.employee.employee_management.dto.DashboardStatisticsResponse;
import com.employee.employee_management.dto.RecentActivityResponse;

import java.util.List;

public interface DashboardService {

    DashboardStatisticsResponse getStatistics();

    List<CategoryCount> getDepartmentDistribution();

    List<CategoryCount> getAttendanceOverview();

    List<CategoryCount> getLeaveOverview();

    List<CategoryAmount> getPayrollOverview(Integer year);

    List<CategoryCount> getPerformanceOverview();

    List<CategoryCount> getEmployeeStatusDistribution();

    List<RecentActivityResponse> getRecentActivities(int limit);
}
