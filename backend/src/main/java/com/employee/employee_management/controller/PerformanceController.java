package com.employee.employee_management.controller;

import com.employee.employee_management.dto.PerformanceRequest;
import com.employee.employee_management.dto.PerformanceResponse;
import com.employee.employee_management.dto.StatusCountResponse;
import com.employee.employee_management.service.PerformanceService;
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
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PerformanceResponse>>> getAll(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long reviewerId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reviewDate,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return ResponseEntity.ok(ApiResponse.success(
                performanceService.getAll(employeeId, reviewerId, rating, pageable)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<PerformanceResponse>>> getEmployeePerformance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.getEmployeePerformance(employeeId)));
    }

    @GetMapping("/report/rating")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<List<StatusCountResponse>>> ratingReport() {
        return ResponseEntity.ok(ApiResponse.success(performanceService.ratingReport()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PerformanceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(performanceService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<PerformanceResponse>> create(@Valid @RequestBody PerformanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Performance review created", performanceService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    public ResponseEntity<ApiResponse<PerformanceResponse>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody PerformanceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Performance review updated", performanceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        performanceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Performance review deleted"));
    }

    private Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, parts[0]);
    }
}
