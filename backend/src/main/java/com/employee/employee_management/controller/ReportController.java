package com.employee.employee_management.controller;

import com.employee.employee_management.dto.report.*;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.entity.LeaveRequestStatus;
import com.employee.employee_management.entity.PaymentStatus;
import com.employee.employee_management.service.ReportService;
import com.employee.employee_management.service.export.CsvExportService;
import com.employee.employee_management.service.export.ExcelExportService;
import com.employee.employee_management.service.export.PdfExportService;
import com.employee.employee_management.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;
    private final CsvExportService csvExportService;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ========== EMPLOYEE REPORTS ==========

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<EmployeeReportDTO>>> employeeReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long designationId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getEmployeeReport(
                departmentId, designationId, status, gender, fromDate, toDate)));
    }

    @GetMapping("/employees/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<EmployeeStatisticsDTO>> employeeStatistics() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getEmployeeStatistics()));
    }

    @GetMapping("/employees/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> employeePdf(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long designationId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<EmployeeReportDTO> data = reportService.getEmployeeReport(departmentId, designationId, status, gender, fromDate, toDate);
        List<String> headers = List.of("Code", "First Name", "Last Name", "Email", "Phone", "Gender",
                "Department", "Designation", "Joining Date", "Salary", "Status");
        List<Object[]> rows = data.stream().map(e -> new Object[]{
                e.getEmployeeCode(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(),
                e.getGender(), e.getDepartmentName(), e.getDesignationName(),
                e.getJoiningDate(), e.getSalary(), Boolean.TRUE.equals(e.getStatus()) ? "Active" : "Inactive"
        }).toList();
        byte[] pdf = pdfExportService.generatePdf("EMPLOYEE REPORT", headers, rows, buildFilterSummary(departmentId, designationId, status, gender, fromDate, toDate));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_report.pdf").body(pdf);
    }

    @GetMapping("/employees/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> employeeExcel(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long designationId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<EmployeeReportDTO> data = reportService.getEmployeeReport(departmentId, designationId, status, gender, fromDate, toDate);
        List<String> headers = List.of("Code", "First Name", "Last Name", "Email", "Phone", "Gender",
                "Department", "Designation", "Joining Date", "Salary", "Status");
        List<Object[]> rows = data.stream().map(e -> new Object[]{
                e.getEmployeeCode(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(),
                e.getGender(), e.getDepartmentName(), e.getDesignationName(),
                e.getJoiningDate(), e.getSalary(), Boolean.TRUE.equals(e.getStatus()) ? "Active" : "Inactive"
        }).toList();
        byte[] excel = excelExportService.generateExcel("EMPLOYEE REPORT", "Employees", headers, rows, buildFilterSummary(departmentId, designationId, status, gender, fromDate, toDate));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_report.xlsx").body(excel);
    }

    @GetMapping("/employees/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> employeeCsv(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long designationId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<EmployeeReportDTO> data = reportService.getEmployeeReport(departmentId, designationId, status, gender, fromDate, toDate);
        List<String> headers = List.of("Code", "First Name", "Last Name", "Email", "Phone", "Gender",
                "Department", "Designation", "Joining Date", "Salary", "Status");
        List<Object[]> rows = data.stream().map(e -> new Object[]{
                e.getEmployeeCode(), e.getFirstName(), e.getLastName(), e.getEmail(), e.getPhone(),
                e.getGender(), e.getDepartmentName(), e.getDesignationName(),
                e.getJoiningDate(), e.getSalary(), Boolean.TRUE.equals(e.getStatus()) ? "Active" : "Inactive"
        }).toList();
        byte[] csv = csvExportService.generateCsv(headers, rows);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_report.csv").body(csv);
    }

    // ========== DEPARTMENT REPORTS ==========

    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<DepartmentReportDTO>>> departmentReport(
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDepartmentReport(status)));
    }

    @GetMapping("/departments/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> departmentPdf(@RequestParam(required = false) Boolean status) {
        List<DepartmentReportDTO> data = reportService.getDepartmentReport(status);
        List<String> headers = List.of("Department", "Description", "Status", "Employees", "Active", "Inactive", "Total Salary");
        List<Object[]> rows = data.stream().map(d -> new Object[]{
                d.getName(), d.getDescription(), Boolean.TRUE.equals(d.getStatus()) ? "Active" : "Inactive",
                d.getEmployeeCount(), d.getActiveEmployees(), d.getInactiveEmployees(), d.getTotalSalary()
        }).toList();
        byte[] pdf = pdfExportService.generatePdf("DEPARTMENT REPORT", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_report.pdf").body(pdf);
    }

    @GetMapping("/departments/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> departmentExcel(@RequestParam(required = false) Boolean status) {
        List<DepartmentReportDTO> data = reportService.getDepartmentReport(status);
        List<String> headers = List.of("Department", "Description", "Status", "Employees", "Active", "Inactive", "Total Salary");
        List<Object[]> rows = data.stream().map(d -> new Object[]{
                d.getName(), d.getDescription(), Boolean.TRUE.equals(d.getStatus()) ? "Active" : "Inactive",
                d.getEmployeeCount(), d.getActiveEmployees(), d.getInactiveEmployees(), d.getTotalSalary()
        }).toList();
        byte[] excel = excelExportService.generateExcel("DEPARTMENT REPORT", "Departments", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_report.xlsx").body(excel);
    }

    @GetMapping("/departments/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> departmentCsv(@RequestParam(required = false) Boolean status) {
        List<DepartmentReportDTO> data = reportService.getDepartmentReport(status);
        List<String> headers = List.of("Department", "Description", "Status", "Employees", "Active", "Inactive", "Total Salary");
        List<Object[]> rows = data.stream().map(d -> new Object[]{
                d.getName(), d.getDescription(), Boolean.TRUE.equals(d.getStatus()) ? "Active" : "Inactive",
                d.getEmployeeCount(), d.getActiveEmployees(), d.getInactiveEmployees(), d.getTotalSalary()
        }).toList();
        byte[] csv = csvExportService.generateCsv(headers, rows);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=department_report.csv").body(csv);
    }

    // ========== DESIGNATION REPORTS ==========

    @GetMapping("/designations")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<DesignationReportDTO>>> designationReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDesignationReport(departmentId, status)));
    }

    @GetMapping("/designations/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> designationPdf(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean status) {
        List<DesignationReportDTO> data = reportService.getDesignationReport(departmentId, status);
        List<String> headers = List.of("Designation", "Department", "Status", "Employees", "Total Salary");
        List<Object[]> rows = data.stream().map(d -> new Object[]{
                d.getName(), d.getDepartmentName(), Boolean.TRUE.equals(d.getStatus()) ? "Active" : "Inactive",
                d.getEmployeeCount(), d.getTotalSalary()
        }).toList();
        byte[] pdf = pdfExportService.generatePdf("DESIGNATION REPORT", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=designation_report.pdf").body(pdf);
    }

    @GetMapping("/designations/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> designationExcel(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean status) {
        List<DesignationReportDTO> data = reportService.getDesignationReport(departmentId, status);
        List<String> headers = List.of("Designation", "Department", "Status", "Employees", "Total Salary");
        List<Object[]> rows = data.stream().map(d -> new Object[]{
                d.getName(), d.getDepartmentName(), Boolean.TRUE.equals(d.getStatus()) ? "Active" : "Inactive",
                d.getEmployeeCount(), d.getTotalSalary()
        }).toList();
        byte[] excel = excelExportService.generateExcel("DESIGNATION REPORT", "Designations", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=designation_report.xlsx").body(excel);
    }

    @GetMapping("/designations/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<byte[]> designationCsv(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean status) {
        List<DesignationReportDTO> data = reportService.getDesignationReport(departmentId, status);
        List<String> headers = List.of("Designation", "Department", "Status", "Employees", "Total Salary");
        List<Object[]> rows = data.stream().map(d -> new Object[]{
                d.getName(), d.getDepartmentName(), Boolean.TRUE.equals(d.getStatus()) ? "Active" : "Inactive",
                d.getEmployeeCount(), d.getTotalSalary()
        }).toList();
        byte[] csv = csvExportService.generateCsv(headers, rows);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=designation_report.csv").body(csv);
    }

    // ========== ATTENDANCE REPORTS ==========

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<AttendanceReportDTO>>> attendanceReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAttendanceReport(
                employeeId, departmentId, status, fromDate, toDate)));
    }

    @GetMapping("/attendance/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<MonthlyAttendanceReportDTO>>> monthlyAttendance(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getMonthlyAttendanceReport(month, year, departmentId, employeeId)));
    }

    @GetMapping("/attendance/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceStatisticsDTO>> attendanceStatistics(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAttendanceStatistics(employeeId, departmentId, month, year)));
    }

    @GetMapping("/attendance/overtime")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<OvertimeReportDTO>>> overtimeReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getOvertimeReport(departmentId, fromDate, toDate)));
    }

    @GetMapping("/attendance/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> attendancePdf(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<AttendanceReportDTO> data = reportService.getAttendanceReport(employeeId, departmentId, status, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Date", "Check In", "Check Out", "Hours", "Overtime", "Status");
        List<Object[]> rows = data.stream().map(a -> new Object[]{
                a.getEmployeeCode(), a.getEmployeeName(), a.getDepartmentName(), a.getDesignationName(),
                a.getAttendanceDate(), a.getCheckIn(), a.getCheckOut(), a.getWorkingHours(), a.getOvertime(), a.getStatus()
        }).toList();
        byte[] pdf = pdfExportService.generatePdf("ATTENDANCE REPORT", headers, rows, buildAttendanceFilterSummary(departmentId, status, fromDate, toDate));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.pdf").body(pdf);
    }

    @GetMapping("/attendance/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> attendanceExcel(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<AttendanceReportDTO> data = reportService.getAttendanceReport(employeeId, departmentId, status, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Date", "Check In", "Check Out", "Hours", "Overtime", "Status");
        List<Object[]> rows = data.stream().map(a -> new Object[]{
                a.getEmployeeCode(), a.getEmployeeName(), a.getDepartmentName(), a.getDesignationName(),
                a.getAttendanceDate(), a.getCheckIn(), a.getCheckOut(), a.getWorkingHours(), a.getOvertime(), a.getStatus()
        }).toList();
        byte[] excel = excelExportService.generateExcel("ATTENDANCE REPORT", "Attendance", headers, rows, buildAttendanceFilterSummary(departmentId, status, fromDate, toDate));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.xlsx").body(excel);
    }

    @GetMapping("/attendance/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> attendanceCsv(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<AttendanceReportDTO> data = reportService.getAttendanceReport(employeeId, departmentId, status, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Date", "Check In", "Check Out", "Hours", "Overtime", "Status");
        List<Object[]> rows = data.stream().map(a -> new Object[]{
                a.getEmployeeCode(), a.getEmployeeName(), a.getDepartmentName(), a.getDesignationName(),
                a.getAttendanceDate(), a.getCheckIn(), a.getCheckOut(), a.getWorkingHours(), a.getOvertime(), a.getStatus()
        }).toList();
        byte[] csv = csvExportService.generateCsv(headers, rows);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_report.csv").body(csv);
    }

    // ========== LEAVE REPORTS ==========

    @GetMapping("/leaves")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<LeaveReportDTO>>> leaveReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getLeaveReport(
                employeeId, departmentId, leaveTypeId, status, fromDate, toDate)));
    }

    @GetMapping("/leaves/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveStatisticsDTO>> leaveStatistics(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getLeaveStatistics(departmentId)));
    }

    @GetMapping("/leaves/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<LeaveBalanceReportDTO>>> leaveBalance(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getLeaveBalanceReport(departmentId, year)));
    }

    @GetMapping("/leaves/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> leavePdf(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<LeaveReportDTO> data = reportService.getLeaveReport(employeeId, departmentId, leaveTypeId, status, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Type", "Start", "End", "Days", "Reason", "Status", "Approved By");
        List<Object[]> rows = data.stream().map(l -> new Object[]{
                l.getEmployeeCode(), l.getEmployeeName(), l.getDepartmentName(), l.getLeaveTypeName(),
                l.getStartDate(), l.getEndDate(), l.getTotalDays(), l.getReason(), l.getStatus(), l.getApprovedByName()
        }).toList();
        byte[] pdf = pdfExportService.generatePdf("LEAVE REPORT", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave_report.pdf").body(pdf);
    }

    @GetMapping("/leaves/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> leaveExcel(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<LeaveReportDTO> data = reportService.getLeaveReport(employeeId, departmentId, leaveTypeId, status, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Type", "Start", "End", "Days", "Reason", "Status", "Approved By");
        List<Object[]> rows = data.stream().map(l -> new Object[]{
                l.getEmployeeCode(), l.getEmployeeName(), l.getDepartmentName(), l.getLeaveTypeName(),
                l.getStartDate(), l.getEndDate(), l.getTotalDays(), l.getReason(), l.getStatus(), l.getApprovedByName()
        }).toList();
        byte[] excel = excelExportService.generateExcel("LEAVE REPORT", "Leaves", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave_report.xlsx").body(excel);
    }

    @GetMapping("/leaves/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> leaveCsv(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<LeaveReportDTO> data = reportService.getLeaveReport(employeeId, departmentId, leaveTypeId, status, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Type", "Start", "End", "Days", "Reason", "Status", "Approved By");
        List<Object[]> rows = data.stream().map(l -> new Object[]{
                l.getEmployeeCode(), l.getEmployeeName(), l.getDepartmentName(), l.getLeaveTypeName(),
                l.getStartDate(), l.getEndDate(), l.getTotalDays(), l.getReason(), l.getStatus(), l.getApprovedByName()
        }).toList();
        byte[] csv = csvExportService.generateCsv(headers, rows);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leave_report.csv").body(csv);
    }

    // ========== PAYROLL REPORTS ==========

    @GetMapping("/payroll")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<PayrollReportDTO>>> payrollReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus paymentStatus) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPayrollReport(
                employeeId, departmentId, month, year, paymentStatus)));
    }

    @GetMapping("/payroll/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PayrollStatisticsDTO>> payrollStatistics(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPayrollStatistics(month, year, departmentId)));
    }

    @GetMapping("/payroll/department")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<DepartmentPayrollReportDTO>>> departmentPayroll(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getDepartmentPayrollReport(month, year)));
    }

    @GetMapping("/payroll/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<PaymentStatusReportDTO>>> paymentStatusReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPaymentStatusReport(month, year)));
    }

    @GetMapping("/payroll/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<byte[]> payrollPdf(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus paymentStatus) {
        List<PayrollReportDTO> data = reportService.getPayrollReport(employeeId, departmentId, month, year, paymentStatus);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Month", "Year",
                "Basic", "Allowances", "Overtime", "Bonus", "Gross", "Tax", "Deduction", "Net", "Status");
        List<Object[]> rows = data.stream().map(p -> new Object[]{
                p.getEmployeeCode(), p.getEmployeeName(), p.getDepartmentName(), p.getDesignationName(),
                p.getMonth(), p.getYear(), p.getBasicSalary(),
                p.getHouseAllowance().add(p.getTransportAllowance()).add(p.getMedicalAllowance()),
                p.getOvertime(), p.getBonus(), p.getGrossSalary(), p.getTax(), p.getDeduction(), p.getNetSalary(),
                p.getPaymentStatus()
        }).toList();
        byte[] pdf = pdfExportService.generatePdf("PAYROLL REPORT", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll_report.pdf").body(pdf);
    }

    @GetMapping("/payroll/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<byte[]> payrollExcel(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus paymentStatus) {
        List<PayrollReportDTO> data = reportService.getPayrollReport(employeeId, departmentId, month, year, paymentStatus);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Month", "Year",
                "Basic", "Allowances", "Overtime", "Bonus", "Gross", "Tax", "Deduction", "Net", "Status");
        List<Object[]> rows = data.stream().map(p -> new Object[]{
                p.getEmployeeCode(), p.getEmployeeName(), p.getDepartmentName(), p.getDesignationName(),
                p.getMonth(), p.getYear(), p.getBasicSalary(),
                p.getHouseAllowance().add(p.getTransportAllowance()).add(p.getMedicalAllowance()),
                p.getOvertime(), p.getBonus(), p.getGrossSalary(), p.getTax(), p.getDeduction(), p.getNetSalary(),
                p.getPaymentStatus()
        }).toList();
        byte[] excel = excelExportService.generateExcel("PAYROLL REPORT", "Payroll", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll_report.xlsx").body(excel);
    }

    @GetMapping("/payroll/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<byte[]> payrollCsv(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus paymentStatus) {
        List<PayrollReportDTO> data = reportService.getPayrollReport(employeeId, departmentId, month, year, paymentStatus);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Month", "Year",
                "Basic", "Allowances", "Overtime", "Bonus", "Gross", "Tax", "Deduction", "Net", "Status");
        List<Object[]> rows = data.stream().map(p -> new Object[]{
                p.getEmployeeCode(), p.getEmployeeName(), p.getDepartmentName(), p.getDesignationName(),
                p.getMonth(), p.getYear(), p.getBasicSalary(),
                p.getHouseAllowance().add(p.getTransportAllowance()).add(p.getMedicalAllowance()),
                p.getOvertime(), p.getBonus(), p.getGrossSalary(), p.getTax(), p.getDeduction(), p.getNetSalary(),
                p.getPaymentStatus()
        }).toList();
        byte[] csv = csvExportService.generateCsv(headers, rows);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payroll_report.csv").body(csv);
    }

    // ========== PERFORMANCE REPORTS ==========

    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<PerformanceReportDTO>>> performanceReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPerformanceReport(
                employeeId, departmentId, rating, fromDate, toDate)));
    }

    @GetMapping("/performance/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<PerformanceStatisticsDTO>> performanceStatistics(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getPerformanceStatistics(departmentId)));
    }

    @GetMapping("/performance/export/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> performancePdf(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<PerformanceReportDTO> data = reportService.getPerformanceReport(employeeId, departmentId, rating, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Reviewer", "Date", "Rating", "Strengths", "Weaknesses", "Comments");
        List<Object[]> rows = data.stream().map(p -> new Object[]{
                p.getEmployeeCode(), p.getEmployeeName(), p.getDepartmentName(), p.getDesignationName(),
                p.getReviewerName(), p.getReviewDate(), p.getRating(), p.getStrengths(), p.getWeaknesses(), p.getComments()
        }).toList();
        byte[] pdf = pdfExportService.generatePdf("PERFORMANCE REPORT", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=performance_report.pdf").body(pdf);
    }

    @GetMapping("/performance/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> performanceExcel(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<PerformanceReportDTO> data = reportService.getPerformanceReport(employeeId, departmentId, rating, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Reviewer", "Date", "Rating", "Strengths", "Weaknesses", "Comments");
        List<Object[]> rows = data.stream().map(p -> new Object[]{
                p.getEmployeeCode(), p.getEmployeeName(), p.getDepartmentName(), p.getDesignationName(),
                p.getReviewerName(), p.getReviewDate(), p.getRating(), p.getStrengths(), p.getWeaknesses(), p.getComments()
        }).toList();
        byte[] excel = excelExportService.generateExcel("PERFORMANCE REPORT", "Performance", headers, rows, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=performance_report.xlsx").body(excel);
    }

    @GetMapping("/performance/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<byte[]> performanceCsv(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<PerformanceReportDTO> data = reportService.getPerformanceReport(employeeId, departmentId, rating, fromDate, toDate);
        List<String> headers = List.of("Code", "Employee", "Department", "Designation", "Reviewer", "Date", "Rating", "Strengths", "Weaknesses", "Comments");
        List<Object[]> rows = data.stream().map(p -> new Object[]{
                p.getEmployeeCode(), p.getEmployeeName(), p.getDepartmentName(), p.getDesignationName(),
                p.getReviewerName(), p.getReviewDate(), p.getRating(), p.getStrengths(), p.getWeaknesses(), p.getComments()
        }).toList();
        byte[] csv = csvExportService.generateCsv(headers, rows);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=performance_report.csv").body(csv);
    }

    // ========== HELPER METHODS ==========

    private String buildFilterSummary(Long departmentId, Long designationId, Boolean status,
                                       String gender, LocalDate fromDate, LocalDate toDate) {
        StringBuilder sb = new StringBuilder();
        if (departmentId != null) sb.append("Department ID: ").append(departmentId).append("; ");
        if (designationId != null) sb.append("Designation ID: ").append(designationId).append("; ");
        if (status != null) sb.append("Status: ").append(Boolean.TRUE.equals(status) ? "Active" : "Inactive").append("; ");
        if (gender != null) sb.append("Gender: ").append(gender).append("; ");
        if (fromDate != null) sb.append("From: ").append(fromDate.format(DF)).append("; ");
        if (toDate != null) sb.append("To: ").append(toDate.format(DF)).append("; ");
        return sb.isEmpty() ? null : sb.toString();
    }

    private String buildAttendanceFilterSummary(Long departmentId, AttendanceStatus status,
                                                 LocalDate fromDate, LocalDate toDate) {
        StringBuilder sb = new StringBuilder();
        if (departmentId != null) sb.append("Department ID: ").append(departmentId).append("; ");
        if (status != null) sb.append("Status: ").append(status).append("; ");
        if (fromDate != null) sb.append("From: ").append(fromDate.format(DF)).append("; ");
        if (toDate != null) sb.append("To: ").append(toDate.format(DF)).append("; ");
        return sb.isEmpty() ? null : sb.toString();
    }
}
