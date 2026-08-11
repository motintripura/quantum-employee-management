package com.employee.employee_management.controller;

import com.employee.employee_management.dto.LeaveBalanceResponse;
import com.employee.employee_management.service.LeaveService;
import com.employee.employee_management.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveService leaveService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getBalances(
            @RequestParam Long employeeId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getBalances(employeeId, year)));
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<String>> initialize(
            @RequestParam Long employeeId,
            @RequestParam(required = false) Integer year) {
        leaveService.initializeBalances(employeeId, year);
        return ResponseEntity.ok(ApiResponse.success("Leave balances initialized"));
    }
}
