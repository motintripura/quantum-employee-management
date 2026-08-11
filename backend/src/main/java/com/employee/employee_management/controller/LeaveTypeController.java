package com.employee.employee_management.controller;

import com.employee.employee_management.dto.LeaveTypeRequest;
import com.employee.employee_management.dto.LeaveTypeResponse;
import com.employee.employee_management.service.LeaveService;
import com.employee.employee_management.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveService leaveService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveTypes()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<LeaveTypeResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getActiveLeaveTypes()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> create(@Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave type created", leaveService.createLeaveType(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Leave type updated", leaveService.updateLeaveType(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Leave type status updated", leaveService.toggleLeaveTypeStatus(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        leaveService.deleteLeaveType(id);
        return ResponseEntity.ok(ApiResponse.success("Leave type deleted"));
    }
}
