package com.employee.employee_management.mapper;

import com.employee.employee_management.dto.AttendanceResponse;
import com.employee.employee_management.entity.Attendance;
import com.employee.employee_management.entity.Employee;

public final class AttendanceMapper {

    private AttendanceMapper() {
    }

    public static AttendanceResponse toResponse(Attendance attendance) {
        Employee employee = attendance.getEmployee();
        String name = employee.getLastName() == null || employee.getLastName().isBlank()
                ? employee.getFirstName()
                : employee.getFirstName() + " " + employee.getLastName();
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(name)
                .departmentId(employee.getDepartment().getId())
                .departmentName(employee.getDepartment().getName())
                .attendanceDate(attendance.getAttendanceDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .workingHours(attendance.getWorkingHours())
                .overtime(attendance.getOvertime())
                .status(attendance.getStatus())
                .remarks(attendance.getRemarks())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}
