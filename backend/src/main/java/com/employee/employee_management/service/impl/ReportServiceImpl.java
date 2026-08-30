package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.report.*;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.LeaveRequestStatus;
import com.employee.employee_management.entity.PaymentStatus;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.repository.ReportRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // ========== EMPLOYEE REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeReportDTO> getEmployeeReport(Long departmentId, Long designationId, Boolean status,
                                                      String gender, LocalDate fromDate, LocalDate toDate) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getEmployeeReport(departmentId, designationId, status, gender,
                fromDate, toDate, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeStatisticsDTO getEmployeeStatistics() {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getEmployeeStatistics(managerDepartmentId);
    }

    // ========== DEPARTMENT REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentReportDTO> getDepartmentReport(Boolean status) {
        return reportRepository.getDepartmentReport(status);
    }

    // ========== DESIGNATION REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<DesignationReportDTO> getDesignationReport(Long departmentId, Boolean status) {
        return reportRepository.getDesignationReport(departmentId, status);
    }

    // ========== ATTENDANCE REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceReportDTO> getAttendanceReport(Long employeeId, Long departmentId,
                                                          AttendanceStatus status, LocalDate fromDate,
                                                          LocalDate toDate) {
        Long managerDepartmentId = getManagerDepartmentId();
        Long allowedEmployeeId = getAllowedEmployeeId();
        Long effectiveEmployeeId = allowedEmployeeId != null ? allowedEmployeeId : employeeId;
        return reportRepository.getAttendanceReport(effectiveEmployeeId, departmentId, status,
                fromDate, toDate, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyAttendanceReportDTO> getMonthlyAttendanceReport(Integer month, Integer year,
                                                                        Long departmentId, Long employeeId) {
        Long managerDepartmentId = getManagerDepartmentId();
        Long allowedEmployeeId = getAllowedEmployeeId();
        Long effectiveEmployeeId = allowedEmployeeId != null ? allowedEmployeeId : employeeId;
        return reportRepository.getMonthlyAttendanceReport(month, year, departmentId,
                effectiveEmployeeId, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatisticsDTO getAttendanceStatistics(Long employeeId, Long departmentId,
                                                           Integer month, Integer year) {
        Long managerDepartmentId = getManagerDepartmentId();
        Long allowedEmployeeId = getAllowedEmployeeId();
        Long effectiveEmployeeId = allowedEmployeeId != null ? allowedEmployeeId : employeeId;
        return reportRepository.getAttendanceStatistics(effectiveEmployeeId, departmentId,
                month, year, managerDepartmentId);
    }

    // ========== OVERTIME REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<OvertimeReportDTO> getOvertimeReport(Long departmentId, LocalDate fromDate,
                                                      LocalDate toDate) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getOvertimeReport(departmentId, fromDate, toDate, managerDepartmentId);
    }

    // ========== LEAVE REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<LeaveReportDTO> getLeaveReport(Long employeeId, Long departmentId, Long leaveTypeId,
                                                LeaveRequestStatus status, LocalDate fromDate,
                                                LocalDate toDate) {
        Long managerDepartmentId = getManagerDepartmentId();
        Long allowedEmployeeId = getAllowedEmployeeId();
        Long effectiveEmployeeId = allowedEmployeeId != null ? allowedEmployeeId : employeeId;
        return reportRepository.getLeaveReport(effectiveEmployeeId, departmentId, leaveTypeId,
                status, fromDate, toDate, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveStatisticsDTO getLeaveStatistics(Long departmentId) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getLeaveStatistics(departmentId, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceReportDTO> getLeaveBalanceReport(Long departmentId, Integer year) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getLeaveBalanceReport(departmentId, year, managerDepartmentId);
    }

    // ========== PAYROLL REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<PayrollReportDTO> getPayrollReport(Long employeeId, Long departmentId, Integer month,
                                                    Integer year, PaymentStatus paymentStatus) {
        Long managerDepartmentId = getManagerDepartmentId();
        Long allowedEmployeeId = getAllowedEmployeeId();
        Long effectiveEmployeeId = allowedEmployeeId != null ? allowedEmployeeId : employeeId;
        return reportRepository.getPayrollReport(effectiveEmployeeId, departmentId, month, year,
                paymentStatus, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public PayrollStatisticsDTO getPayrollStatistics(Integer month, Integer year, Long departmentId) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getPayrollStatistics(month, year, departmentId, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentPayrollReportDTO> getDepartmentPayrollReport(Integer month, Integer year) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getDepartmentPayrollReport(month, year, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentStatusReportDTO> getPaymentStatusReport(Integer month, Integer year) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getPaymentStatusReport(month, year, managerDepartmentId);
    }

    // ========== PERFORMANCE REPORTS ==========

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceReportDTO> getPerformanceReport(Long employeeId, Long departmentId,
                                                            Integer rating, LocalDate fromDate,
                                                            LocalDate toDate) {
        Long managerDepartmentId = getManagerDepartmentId();
        Long allowedEmployeeId = getAllowedEmployeeId();
        Long effectiveEmployeeId = allowedEmployeeId != null ? allowedEmployeeId : employeeId;
        return reportRepository.getPerformanceReport(effectiveEmployeeId, departmentId, rating,
                fromDate, toDate, managerDepartmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceStatisticsDTO getPerformanceStatistics(Long departmentId) {
        Long managerDepartmentId = getManagerDepartmentId();
        return reportRepository.getPerformanceStatistics(departmentId, managerDepartmentId);
    }

    // ========== HELPER METHODS ==========

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private Long getManagerDepartmentId() {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.MANAGER) {
            return null;
        }
        Employee employee = user.getEmployee();
        if (employee == null || employee.getDepartment() == null) {
            return null;
        }
        return employee.getDepartment().getId();
    }

    private Long getCurrentEmployeeId() {
        User user = getCurrentUser();
        if (user == null || user.getRole() != Role.EMPLOYEE) {
            return null;
        }
        Employee employee = user.getEmployee();
        if (employee == null) {
            return null;
        }
        return employee.getId();
    }

    private Long getAllowedEmployeeId() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        if (user.getRole() == Role.EMPLOYEE) {
            Employee employee = user.getEmployee();
            return employee != null ? employee.getId() : null;
        }
        return null;
    }
}
