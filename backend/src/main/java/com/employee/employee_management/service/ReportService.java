package com.employee.employee_management.service;

import com.employee.employee_management.dto.report.*;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.entity.LeaveRequestStatus;
import com.employee.employee_management.entity.PaymentStatus;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    List<EmployeeReportDTO> getEmployeeReport(Long departmentId, Long designationId, Boolean status,
                                              String gender, LocalDate fromDate, LocalDate toDate);

    EmployeeStatisticsDTO getEmployeeStatistics();

    List<DepartmentReportDTO> getDepartmentReport(Boolean status);

    List<DesignationReportDTO> getDesignationReport(Long departmentId, Boolean status);

    List<AttendanceReportDTO> getAttendanceReport(Long employeeId, Long departmentId,
                                                   AttendanceStatus status, LocalDate fromDate,
                                                   LocalDate toDate);

    List<MonthlyAttendanceReportDTO> getMonthlyAttendanceReport(Integer month, Integer year,
                                                                 Long departmentId, Long employeeId);

    AttendanceStatisticsDTO getAttendanceStatistics(Long employeeId, Long departmentId,
                                                    Integer month, Integer year);

    List<OvertimeReportDTO> getOvertimeReport(Long departmentId, LocalDate fromDate, LocalDate toDate);

    List<LeaveReportDTO> getLeaveReport(Long employeeId, Long departmentId, Long leaveTypeId,
                                         LeaveRequestStatus status, LocalDate fromDate, LocalDate toDate);

    LeaveStatisticsDTO getLeaveStatistics(Long departmentId);

    List<LeaveBalanceReportDTO> getLeaveBalanceReport(Long departmentId, Integer year);

    List<PayrollReportDTO> getPayrollReport(Long employeeId, Long departmentId, Integer month,
                                             Integer year, PaymentStatus paymentStatus);

    PayrollStatisticsDTO getPayrollStatistics(Integer month, Integer year, Long departmentId);

    List<DepartmentPayrollReportDTO> getDepartmentPayrollReport(Integer month, Integer year);

    List<PaymentStatusReportDTO> getPaymentStatusReport(Integer month, Integer year);

    List<PerformanceReportDTO> getPerformanceReport(Long employeeId, Long departmentId,
                                                     Integer rating, LocalDate fromDate, LocalDate toDate);

    PerformanceStatisticsDTO getPerformanceStatistics(Long departmentId);
}
