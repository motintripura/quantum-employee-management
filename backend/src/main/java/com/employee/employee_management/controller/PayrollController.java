package com.employee.employee_management.controller;

import com.employee.employee_management.dto.PayrollRequest;
import com.employee.employee_management.dto.PayrollResponse;
import com.employee.employee_management.dto.PayslipResponse;
import com.employee.employee_management.entity.PaymentStatus;
import com.employee.employee_management.service.PayrollService;
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

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PayrollResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "year,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(
                payrollService.getAll(employeeId, month, year, paymentStatus, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getEmployeeHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getEmployeeHistory(employeeId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getById(id)));
    }

    @GetMapping("/{id}/payslip")
    public ResponseEntity<ApiResponse<PayslipResponse>> getPayslip(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payrollService.getPayslip(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PayrollResponse>> create(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payroll created successfully", payrollService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PayrollResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payroll updated successfully", payrollService.update(id, request)));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<PayrollResponse>> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payroll marked as paid", payrollService.markAsPaid(id)));
    }

    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, parts[0]);
    }
}
