package com.employee.employee_management.service;

import com.employee.employee_management.dto.*;
import com.employee.employee_management.entity.LeaveRequestStatus;
import com.employee.employee_management.util.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LeaveService {

    PageResponse<LeaveResponse> getAll(Long employeeId,
                                       Long leaveTypeId,
                                       LeaveRequestStatus status,
                                       LocalDate fromDate,
                                       LocalDate toDate,
                                       Pageable pageable);

    List<LeaveResponse> getEmployeeLeaves(Long employeeId);

    LeaveResponse getById(Long id);

    LeaveResponse apply(LeaveApplyRequest request);

    LeaveResponse approve(Long id);

    LeaveResponse reject(Long id);

    LeaveResponse cancel(Long id);

    List<LeaveBalanceResponse> getBalances(Long employeeId, Integer year);

    void initializeBalances(Long employeeId, Integer year);

    List<LeaveTypeResponse> getLeaveTypes();

    List<LeaveTypeResponse> getActiveLeaveTypes();

    LeaveTypeResponse createLeaveType(LeaveTypeRequest request);

    LeaveTypeResponse updateLeaveType(Long id, LeaveTypeRequest request);

    void deleteLeaveType(Long id);

    LeaveTypeResponse toggleLeaveTypeStatus(Long id);

    List<StatusCountResponse> statusReport();
}
