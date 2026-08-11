package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.*;
import com.employee.employee_management.entity.*;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ForbiddenException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.mapper.LeaveMapper;
import com.employee.employee_management.repository.*;
import com.employee.employee_management.service.LeaveService;
import com.employee.employee_management.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> getAll(Long employeeId,
                                              Long leaveTypeId,
                                              LeaveRequestStatus status,
                                              LocalDate fromDate,
                                              LocalDate toDate,
                                              Pageable pageable) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE) {
            Long ownId = user.getEmployee().getId();
            if (employeeId != null && !employeeId.equals(ownId)) {
                throw new ForbiddenException("Employees can only view their own leave requests");
            }
            employeeId = ownId;
        }
        Page<LeaveRequest> page = leaveRequestRepository.search(employeeId, leaveTypeId, status, fromDate, toDate, pageable);
        return PageResponse.from(page.map(LeaveMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getEmployeeLeaves(Long employeeId) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !employeeId.equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only view their own leave requests");
        }
        findEmployee(employeeId);
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream().map(LeaveMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getById(Long id) {
        LeaveRequest request = findRequest(id);
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !request.getEmployee().getId().equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only view their own leave requests");
        }
        return LeaveMapper.toResponse(request);
    }

    @Override
    @Transactional
    public LeaveResponse apply(LeaveApplyRequest request) {
        Long employeeId = resolveEmployeeId(request.getEmployeeId());
        Employee employee = findEmployee(employeeId);
        LeaveType leaveType = findLeaveType(request.getLeaveTypeId());

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Leave start date cannot be in the past");
        }

        BigDecimal totalDays = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1);

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
                employeeId, request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Employee already has a pending or approved leave for the selected dates");
        }

        ensureBalances(employeeId, request.getStartDate().getYear());
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveType.getId(), request.getStartDate().getYear())
                .orElseThrow(() -> new BadRequestException("No leave balance available for this leave type"));

        if (balance.getRemainingDays().compareTo(totalDays) < 0) {
            throw new BadRequestException("Insufficient leave balance. Available: "
                    + balance.getRemainingDays() + " days, requested: " + totalDays + " days");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveRequestStatus.PENDING)
                .build();
        leaveRequestRepository.save(leaveRequest);
        return LeaveMapper.toResponse(leaveRequest);
    }

    @Override
    @Transactional
    public LeaveResponse approve(Long id) {
        LeaveRequest request = findRequest(id);
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be approved");
        }

        ensureBalances(request.getEmployee().getId(), request.getStartDate().getYear());
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        request.getEmployee().getId(), request.getLeaveType().getId(), request.getStartDate().getYear())
                .orElseThrow(() -> new BadRequestException("No leave balance available for this leave type"));

        if (balance.getRemainingDays().compareTo(request.getTotalDays()) < 0) {
            throw new BadRequestException("Insufficient leave balance. Available: "
                    + balance.getRemainingDays() + " days");
        }

        User approver = getCurrentUser();
        request.setStatus(LeaveRequestStatus.APPROVED);
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);

        balance.setUsedDays(balance.getUsedDays().add(request.getTotalDays()));
        balance.setRemainingDays(balance.getAllocatedDays().subtract(balance.getUsedDays()));
        leaveBalanceRepository.save(balance);

        return LeaveMapper.toResponse(request);
    }

    @Override
    @Transactional
    public LeaveResponse reject(Long id) {
        LeaveRequest request = findRequest(id);
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be rejected");
        }
        User approver = getCurrentUser();
        request.setStatus(LeaveRequestStatus.REJECTED);
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
        return LeaveMapper.toResponse(request);
    }

    @Override
    @Transactional
    public LeaveResponse cancel(Long id) {
        LeaveRequest request = findRequest(id);
        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be cancelled");
        }
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !request.getEmployee().getId().equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only cancel their own leave requests");
        }
        request.setStatus(LeaveRequestStatus.CANCELLED);
        leaveRequestRepository.save(request);
        return LeaveMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> getBalances(Long employeeId, Integer year) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !employeeId.equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only view their own leave balances");
        }
        findEmployee(employeeId);
        int y = year != null ? year : LocalDate.now().getYear();
        return leaveBalanceRepository.findByEmployeeIdAndYearOrderByLeaveTypeNameAsc(employeeId, y)
                .stream().map(b -> LeaveBalanceResponse.builder()
                        .id(b.getId())
                        .employeeId(b.getEmployee().getId())
                        .employeeName(LeaveMapper.fullName(b.getEmployee()))
                        .leaveTypeId(b.getLeaveType().getId())
                        .leaveTypeName(b.getLeaveType().getName())
                        .year(b.getYear())
                        .allocatedDays(b.getAllocatedDays())
                        .usedDays(b.getUsedDays())
                        .remainingDays(b.getRemainingDays())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void initializeBalances(Long employeeId, Integer year) {
        Employee employee = findEmployee(employeeId);
        int y = year != null ? year : LocalDate.now().getYear();
        ensureBalances(employeeId, y);
        System.out.println("Leave balances initialized for employee " + employee.getEmployeeCode() + " year " + y);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getLeaveTypes() {
        return leaveTypeRepository.findAllByOrderByNameAsc().stream().map(LeaveMapper::toTypeResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> getActiveLeaveTypes() {
        return leaveTypeRepository.findByStatusTrueOrderByNameAsc().stream().map(LeaveMapper::toTypeResponse).toList();
    }

    @Override
    @Transactional
    public LeaveTypeResponse createLeaveType(LeaveTypeRequest request) {
        String name = request.getName().trim().toUpperCase();
        if (leaveTypeRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Leave type already exists: " + name);
        }
        LeaveType leaveType = LeaveType.builder()
                .name(name)
                .description(request.getDescription())
                .defaultDays(request.getDefaultDays() == null ? 0 : request.getDefaultDays())
                .status(request.getStatus() == null ? Boolean.TRUE : request.getStatus())
                .build();
        leaveTypeRepository.save(leaveType);
        return LeaveMapper.toTypeResponse(leaveType);
    }

    @Override
    @Transactional
    public LeaveTypeResponse updateLeaveType(Long id, LeaveTypeRequest request) {
        LeaveType leaveType = findLeaveType(id);
        String name = request.getName().trim().toUpperCase();
        if (leaveTypeRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("Leave type already exists: " + name);
        }
        leaveType.setName(name);
        leaveType.setDescription(request.getDescription());
        if (request.getDefaultDays() != null) {
            leaveType.setDefaultDays(request.getDefaultDays());
        }
        if (request.getStatus() != null) {
            leaveType.setStatus(request.getStatus());
        }
        leaveTypeRepository.save(leaveType);
        return LeaveMapper.toTypeResponse(leaveType);
    }

    @Override
    @Transactional
    public void deleteLeaveType(Long id) {
        LeaveType leaveType = findLeaveType(id);
        if (leaveRequestRepository.countByLeaveTypeId(id) > 0) {
            throw new BadRequestException("Cannot delete leave type '" + leaveType.getName()
                    + "' because leave requests use it");
        }
        leaveTypeRepository.delete(leaveType);
    }

    @Override
    @Transactional
    public LeaveTypeResponse toggleLeaveTypeStatus(Long id) {
        LeaveType leaveType = findLeaveType(id);
        leaveType.setStatus(!Boolean.TRUE.equals(leaveType.getStatus()));
        leaveTypeRepository.save(leaveType);
        return LeaveMapper.toTypeResponse(leaveType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusCountResponse> statusReport() {
        List<StatusCountResponse> result = new ArrayList<>();
        for (Object[] row : leaveRequestRepository.countByStatus()) {
            result.add(StatusCountResponse.builder()
                    .name(String.valueOf(row[0]))
                    .count((Long) row[1])
                    .build());
        }
        return result;
    }

    private void ensureBalances(Long employeeId, int year) {
        if (!leaveBalanceRepository.existsByEmployeeIdAndYear(employeeId, year)) {
            Employee employee = findEmployee(employeeId);
            for (LeaveType type : leaveTypeRepository.findByStatusTrueOrderByNameAsc()) {
                BigDecimal allocated = BigDecimal.valueOf(type.getDefaultDays());
                leaveBalanceRepository.save(LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(type)
                        .year(year)
                        .allocatedDays(allocated)
                        .usedDays(BigDecimal.ZERO)
                        .remainingDays(allocated)
                        .build());
            }
        }
    }

    private Long resolveEmployeeId(Long requestedId) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE) {
            if (requestedId != null && !requestedId.equals(user.getEmployee().getId())) {
                throw new ForbiddenException("Employees can only apply for their own leave");
            }
            return user.getEmployee().getId();
        }
        if (requestedId == null) {
            throw new BadRequestException("employeeId is required");
        }
        return requestedId;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private LeaveType findLeaveType(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found with id: " + id));
    }

    private LeaveRequest findRequest(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
    }
}
