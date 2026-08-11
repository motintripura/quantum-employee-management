package com.employee.employee_management.controller;

import com.employee.employee_management.dto.LeaveApplyRequest;
import com.employee.employee_management.dto.LeaveResponse;
import com.employee.employee_management.dto.StatusCountResponse;
import com.employee.employee_management.entity.LeaveRequestStatus;
import com.employee.employee_management.service.LeaveService;
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
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeaveResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(
                leaveService.getAll(employeeId, leaveTypeId, status, fromDate, toDate, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getEmployeeLeaves(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getEmployeeLeaves(employeeId)));
    }

    @GetMapping("/report/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<StatusCountResponse>>> statusReport() {
        return ResponseEntity.ok(ApiResponse.success(leaveService.statusReport()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveResponse>> apply(@Valid @RequestBody LeaveApplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave request submitted", leaveService.apply(request)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Leave request approved", leaveService.approve(id)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeaveResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected", leaveService.reject(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<LeaveResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled", leaveService.cancel(id)));
    }

    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, parts[0]);
    }
}
