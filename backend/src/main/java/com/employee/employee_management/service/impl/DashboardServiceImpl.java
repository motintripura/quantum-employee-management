package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.CategoryAmount;
import com.employee.employee_management.dto.CategoryCount;
import com.employee.employee_management.dto.DashboardStatisticsResponse;
import com.employee.employee_management.dto.RecentActivityResponse;
import com.employee.employee_management.entity.*;
import com.employee.employee_management.repository.*;
import com.employee.employee_management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final PayrollRepository payrollRepository;
    private final PerformanceReviewRepository performanceReviewRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatisticsResponse getStatistics() {
        LocalDate today = LocalDate.now();
        return DashboardStatisticsResponse.builder()
                .totalEmployees(employeeRepository.count())
                .activeEmployees(employeeRepository.countByStatusTrue())
                .inactiveEmployees(employeeRepository.countByStatusFalse())
                .totalDepartments(departmentRepository.count())
                .totalDesignations(designationRepository.count())
                .totalLeaveTypes(leaveTypeRepository.countByStatusTrue())
                .todayPresent(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.PRESENT))
                .todayAbsent(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.ABSENT))
                .todayLate(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.LATE))
                .todayHalfDay(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.HALF_DAY))
                .todayLeave(attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.LEAVE))
                .pendingLeaveRequests(leaveRequestRepository.countByStatus(LeaveRequestStatus.PENDING))
                .monthlyPayrollTotal(payrollRepository.sumNetSalaryByMonthAndYear(today.getMonthValue(), today.getYear()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryCount> getDepartmentDistribution() {
        List<CategoryCount> result = new ArrayList<>();
        for (Object[] row : employeeRepository.countByDepartment()) {
            result.add(new CategoryCount(String.valueOf(row[0]), (Long) row[1]));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryCount> getAttendanceOverview() {
        List<CategoryCount> result = new ArrayList<>();
        for (Object[] row : attendanceRepository.countByStatusForDate(LocalDate.now())) {
            result.add(new CategoryCount(String.valueOf(row[0]), (Long) row[1]));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryCount> getLeaveOverview() {
        List<CategoryCount> result = new ArrayList<>();
        for (Object[] row : leaveRequestRepository.countByStatus()) {
            result.add(new CategoryCount(String.valueOf(row[0]), (Long) row[1]));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryAmount> getPayrollOverview(Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        Map<Integer, BigDecimal> byMonth = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            byMonth.put(i, BigDecimal.ZERO);
        }
        for (Object[] row : payrollRepository.sumGrossSalaryByMonth(y)) {
            byMonth.put(((Number) row[0]).intValue(), (BigDecimal) row[1]);
        }
        List<CategoryAmount> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            result.add(new CategoryAmount(monthName(i), byMonth.get(i)));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryCount> getPerformanceOverview() {
        List<CategoryCount> result = new ArrayList<>();
        for (Object[] row : performanceReviewRepository.countByRating()) {
            result.add(new CategoryCount("rating_" + row[0], (Long) row[1]));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryCount> getEmployeeStatusDistribution() {
        List<CategoryCount> result = new ArrayList<>();
        result.add(new CategoryCount("ACTIVE", employeeRepository.countByStatusTrue()));
        result.add(new CategoryCount("INACTIVE", employeeRepository.countByStatusFalse()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentActivityResponse> getRecentActivities(int limit) {
        int safeLimit = limit > 0 ? limit : 10;
        PageRequest pageable = PageRequest.of(0, safeLimit);
        List<RecentActivityResponse> activities = new ArrayList<>();

        for (Employee e : employeeRepository.findRecent(pageable)) {
            activities.add(RecentActivityResponse.builder()
                    .type("EMPLOYEE")
                    .description(fullName(e) + " (" + e.getEmployeeCode() + ") joined as "
                            + e.getDesignation().getName() + " in " + e.getDepartment().getName())
                    .timestamp(e.getCreatedAt())
                    .build());
        }

        for (LeaveRequest lr : leaveRequestRepository.findRecent(pageable)) {
            activities.add(RecentActivityResponse.builder()
                    .type("LEAVE")
                    .description(fullName(lr.getEmployee()) + " applied for " + lr.getLeaveType().getName()
                            + " leave (" + lr.getTotalDays() + " day(s)) - " + lr.getStatus())
                    .timestamp(lr.getCreatedAt())
                    .build());
        }

        for (Payroll p : payrollRepository.findRecent(pageable)) {
            activities.add(RecentActivityResponse.builder()
                    .type("PAYROLL")
                    .description("Payroll generated for " + fullName(p.getEmployee()) + " - "
                            + java.time.Month.of(p.getMonth()).name() + " " + p.getYear()
                            + " (" + p.getPaymentStatus() + ")")
                    .timestamp(p.getCreatedAt())
                    .build());
        }

        for (PerformanceReview pr : performanceReviewRepository.findRecent(pageable)) {
            activities.add(RecentActivityResponse.builder()
                    .type("PERFORMANCE")
                    .description("Performance review (" + pr.getRating() + "/5) recorded for "
                            + fullName(pr.getEmployee()))
                    .timestamp(pr.getCreatedAt())
                    .build());
        }

        for (Attendance a : attendanceRepository.findRecent(pageable)) {
            activities.add(RecentActivityResponse.builder()
                    .type("ATTENDANCE")
                    .description(fullName(a.getEmployee()) + " marked " + a.getStatus()
                            + " on " + a.getAttendanceDate())
                    .timestamp(a.getCreatedAt())
                    .build());
        }

        activities.sort(Comparator.comparing(RecentActivityResponse::getTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return activities.stream().limit(safeLimit).toList();
    }

    private String fullName(Employee employee) {
        return employee.getLastName() == null || employee.getLastName().isBlank()
                ? employee.getFirstName()
                : employee.getFirstName() + " " + employee.getLastName();
    }

    private String monthName(int month) {
        return java.time.Month.of(month).name();
    }
}
