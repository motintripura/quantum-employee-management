package com.employee.employee_management.repository;

import com.employee.employee_management.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(Long employeeId, Long leaveTypeId, Integer year);

    List<LeaveBalance> findByEmployeeIdAndYearOrderByLeaveTypeNameAsc(Long employeeId, Integer year);

    List<LeaveBalance> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeIdAndYear(Long employeeId, Integer year);
}
