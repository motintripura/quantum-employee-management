package com.employee.employee_management.controller;

import com.employee.employee_management.dto.*;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.service.AttendanceService;
import com.employee.employee_management.util.ApiResponse;
import com.employee.employee_management.util.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(@RequestBody(required = false) CheckInRequest request) {
        CheckInRequest req = request == null ? new CheckInRequest() : request;
        return ResponseEntity.ok(ApiResponse.success("Checked in successfully", attendanceService.checkIn(req)));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut(@RequestBody(required = false) CheckOutRequest request) {
        CheckOutRequest req = request == null ? new CheckOutRequest() : request;
        return ResponseEntity.ok(ApiResponse.success("Checked out successfully", attendanceService.checkOut(req)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> record(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance recorded successfully", attendanceService.record(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AttendanceResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "attendanceDate,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(
                attendanceService.search(employeeId, departmentId, status, fromDate, toDate, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getEmployeeAttendance(
            @PathVariable Long employeeId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getEmployeeAttendance(employeeId, from, to)));
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<MonthlyAttendanceResponse>>> monthlyReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.monthlyReport(month, year)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getById(id)));
    }

    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, parts[0]);
    }
}
