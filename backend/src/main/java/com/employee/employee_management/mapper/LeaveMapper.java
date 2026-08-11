package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.LeaveResponse;
import com.employee.employee_management.dto.LeaveTypeResponse;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.LeaveRequest;
import com.employee.employee_management.entity.LeaveType;

public final class LeaveMapper {

    private LeaveMapper() {
    }

    public static LeaveResponse toResponse(LeaveRequest request) {
        Employee employee = request.getEmployee();
        String employeeName = fullName(employee);
        return LeaveResponse.builder()
                .id(request.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employeeName)
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getName())
                .leaveTypeId(request.getLeaveType().getId())
                .leaveTypeName(request.getLeaveType().getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(request.getTotalDays())
                .reason(request.getReason())
                .status(request.getStatus())
                .approvedById(request.getApprovedBy() == null ? null : request.getApprovedBy().getId())
                .approvedByUsername(request.getApprovedBy() == null ? null : request.getApprovedBy().getUsername())
                .approvedAt(request.getApprovedAt())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    public static LeaveTypeResponse toTypeResponse(LeaveType leaveType) {
        return LeaveTypeResponse.builder()
                .id(leaveType.getId())
                .name(leaveType.getName())
                .description(leaveType.getDescription())
                .defaultDays(leaveType.getDefaultDays())
                .status(leaveType.getStatus())
                .createdAt(leaveType.getCreatedAt())
                .updatedAt(leaveType.getUpdatedAt())
                .build();
    }

    public static String fullName(Employee employee) {
        return employee.getLastName() == null || employee.getLastName().isBlank()
                ? employee.getFirstName()
                : employee.getFirstName() + " " + employee.getLastName();
    }
}
