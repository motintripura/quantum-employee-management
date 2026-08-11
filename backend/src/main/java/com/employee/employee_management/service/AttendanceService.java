package com.employee.employee_management.service;

import com.employee.employee_management.dto.*;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceResponse checkIn(CheckInRequest request);

    AttendanceResponse checkOut(CheckOutRequest request);

    AttendanceResponse record(AttendanceRequest request);

    AttendanceResponse getById(Long id);

    List<AttendanceResponse> getEmployeeAttendance(Long employeeId, LocalDate from, LocalDate to);

    PageResponse<AttendanceResponse> search(Long employeeId,
                                            Long departmentId,
                                            AttendanceStatus status,
                                            LocalDate fromDate,
                                            LocalDate toDate,
                                            Pageable pageable);

    List<MonthlyAttendanceResponse> monthlyReport(Integer month, Integer year);
}
