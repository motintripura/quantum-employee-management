package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.*;
import com.employee.employee_management.entity.*;
import com.employee.employee_management.exception.BadRequestException;
import com.employee.employee_management.exception.ForbiddenException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.mapper.AttendanceMapper;
import com.employee.employee_management.repository.AttendanceRepository;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.service.AttendanceService;
import com.employee.employee_management.util.Constants;
import com.employee.employee_management.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        Long employeeId = resolveEmployeeId(request.getEmployeeId());
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseGet(() -> {
                    Employee employee = findEmployee(employeeId);
                    return Attendance.builder()
                            .employee(employee)
                            .attendanceDate(today)
                            .workingHours(BigDecimal.ZERO)
                            .overtime(BigDecimal.ZERO)
                            .build();
                });

        if (attendance.getCheckIn() != null) {
            throw new BadRequestException("Employee has already checked in today");
        }

        LocalTime now = LocalTime.now();
        attendance.setCheckIn(now);
        attendance.setStatus(now.isAfter(Constants.LATE_THRESHOLD) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);
        attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(CheckOutRequest request) {
        Long employeeId = resolveEmployeeId(request.getEmployeeId());
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("Employee has not checked in today"));

        if (attendance.getCheckOut() != null) {
            throw new BadRequestException("Employee has already checked out today");
        }
        if (attendance.getCheckIn() == null) {
            throw new BadRequestException("Employee has not checked in today");
        }

        LocalTime now = LocalTime.now();
        attendance.setCheckOut(now);

        long minutes = Duration.between(attendance.getCheckIn(), now).toMinutes();
        if (minutes < 0) {
            minutes += Constants.MINUTES_PER_DAY;
        }
        BigDecimal workingHours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        BigDecimal overtime = workingHours.subtract(BigDecimal.valueOf(Constants.STANDARD_WORK_HOURS));
        if (overtime.signum() < 0) {
            overtime = BigDecimal.ZERO;
        }

        attendance.setWorkingHours(workingHours);
        attendance.setOvertime(overtime);
        if (workingHours.compareTo(BigDecimal.valueOf(Constants.HALF_DAY_HOURS)) < 0) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }
        attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional
    public AttendanceResponse record(AttendanceRequest request) {
        Employee employee = findEmployee(request.getEmployeeId());
        validateTimes(request.getCheckIn(), request.getCheckOut());

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndAttendanceDate(employee.getId(), request.getAttendanceDate())
                .orElseGet(() -> Attendance.builder()
                        .employee(employee)
                        .attendanceDate(request.getAttendanceDate())
                        .workingHours(BigDecimal.ZERO)
                        .overtime(BigDecimal.ZERO)
                        .build());

        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());
        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());

        if (request.getCheckIn() != null && request.getCheckOut() != null) {
            long minutes = Duration.between(request.getCheckIn(), request.getCheckOut()).toMinutes();
            if (minutes < 0) {
                minutes += Constants.MINUTES_PER_DAY;
            }
            BigDecimal workingHours = BigDecimal.valueOf(minutes)
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            attendance.setWorkingHours(workingHours);
            BigDecimal overtime = workingHours.subtract(BigDecimal.valueOf(Constants.STANDARD_WORK_HOURS));
            attendance.setOvertime(overtime.signum() < 0 ? BigDecimal.ZERO : overtime);
        }

        attendanceRepository.save(attendance);
        return AttendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        return AttendanceMapper.toResponse(findAttendance(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getEmployeeAttendance(Long employeeId, LocalDate from, LocalDate to) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && (employeeId == null || !employeeId.equals(user.getEmployee().getId()))) {
            throw new ForbiddenException("Employees can only view their own attendance");
        }
        if (employeeId == null) {
            throw new BadRequestException("employeeId is required");
        }
        findEmployee(employeeId);
        LocalDate start = from != null ? from : LocalDate.of(2000, 1, 1);
        LocalDate end = to != null ? to : LocalDate.now().plusYears(1);
        return attendanceRepository.findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(employeeId, start, end)
                .stream().map(AttendanceMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> search(Long employeeId,
                                                   Long departmentId,
                                                   AttendanceStatus status,
                                                   LocalDate fromDate,
                                                   LocalDate toDate,
                                                   Pageable pageable) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE) {
            Long ownId = user.getEmployee().getId();
            if (employeeId != null && !employeeId.equals(ownId)) {
                throw new ForbiddenException("Employees can only view their own attendance");
            }
            employeeId = ownId;
        }
        Page<Attendance> page = attendanceRepository.search(employeeId, departmentId, status, fromDate, toDate, pageable);
        return PageResponse.from(page.map(AttendanceMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyAttendanceResponse> monthlyReport(Integer month, Integer year) {
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        if (m < 1 || m > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }

        LocalDate start = LocalDate.of(y, m, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);
        List<Attendance> records = attendanceRepository.findByAttendanceDateBetween(start, end);

        User user = getCurrentUser();
        Map<Long, MonthlyAttendanceResponse> summaryMap = new LinkedHashMap<>();

        for (Attendance a : records) {
            Employee e = a.getEmployee();
            if (user.getRole() == Role.EMPLOYEE && !e.getId().equals(user.getEmployee().getId())) {
                continue;
            }
            MonthlyAttendanceResponse summary = summaryMap.computeIfAbsent(e.getId(), k -> buildSummary(e, m, y));
            switch (a.getStatus()) {
                case PRESENT -> summary.setPresentDays(summary.getPresentDays() + 1);
                case ABSENT -> summary.setAbsentDays(summary.getAbsentDays() + 1);
                case LATE -> summary.setLateDays(summary.getLateDays() + 1);
                case HALF_DAY -> summary.setHalfDays(summary.getHalfDays() + 1);
                case LEAVE -> summary.setLeaveDays(summary.getLeaveDays() + 1);
            }
            summary.setTotalDays(summary.getTotalDays() + 1);
            summary.setTotalWorkingHours(summary.getTotalWorkingHours()
                    .add(a.getWorkingHours() == null ? BigDecimal.ZERO : a.getWorkingHours()));
        }

        return new ArrayList<>(summaryMap.values());
    }

    private MonthlyAttendanceResponse buildSummary(Employee e, int month, int year) {
        String name = e.getLastName() == null || e.getLastName().isBlank()
                ? e.getFirstName() : e.getFirstName() + " " + e.getLastName();
        return MonthlyAttendanceResponse.builder()
                .employeeId(e.getId())
                .employeeCode(e.getEmployeeCode())
                .employeeName(name)
                .departmentId(e.getDepartment().getId())
                .departmentName(e.getDepartment().getName())
                .year(year)
                .month(month)
                .totalWorkingHours(BigDecimal.ZERO)
                .build();
    }

    private Long resolveEmployeeId(Long requestedId) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE) {
            if (requestedId != null && !requestedId.equals(user.getEmployee().getId())) {
                throw new ForbiddenException("Employees can only manage their own attendance");
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

    private Attendance findAttendance(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found with id: " + id));
    }

    private void validateTimes(LocalTime checkIn, LocalTime checkOut) {
        if (checkIn != null && checkOut != null && checkOut.isBefore(checkIn)) {
            throw new BadRequestException("Check-out time cannot be before check-in time");
        }
    }
}
