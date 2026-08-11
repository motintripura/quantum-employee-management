package com.employee.employee_management.repository;

import com.employee.employee_management.entity.LeaveRequest;
import com.employee.employee_management.entity.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveRequestStatus status);

    long countByStatus(LeaveRequestStatus status);

    boolean existsByEmployeeIdAndIdNotAndStatus(Long employeeId, Long id, LeaveRequestStatus status);

    long countByLeaveTypeId(Long leaveTypeId);

    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE (:employeeId IS NULL OR lr.employee.id = :employeeId)
              AND (:leaveTypeId IS NULL OR lr.leaveType.id = :leaveTypeId)
              AND (:status IS NULL OR lr.status = :status)
              AND (:fromDate IS NULL OR lr.startDate >= :fromDate)
              AND (:toDate IS NULL OR lr.endDate <= :toDate)
            """)
    Page<LeaveRequest> search(@Param("employeeId") Long employeeId,
                              @Param("leaveTypeId") Long leaveTypeId,
                              @Param("status") LeaveRequestStatus status,
                              @Param("fromDate") LocalDate fromDate,
                              @Param("toDate") LocalDate toDate,
                              Pageable pageable);

    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.employee.id = :employeeId
              AND lr.status IN (com.employee.employee_management.entity.LeaveRequestStatus.PENDING,
                                com.employee.employee_management.entity.LeaveRequestStatus.APPROVED)
              AND lr.startDate <= :endDate
              AND lr.endDate >= :startDate
            """)
    List<LeaveRequest> findOverlapping(@Param("employeeId") Long employeeId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT lr FROM LeaveRequest lr
            WHERE lr.employee.id = :employeeId
              AND lr.leaveType.id = :leaveTypeId
              AND lr.status = com.employee.employee_management.entity.LeaveRequestStatus.APPROVED
              AND lr.startDate BETWEEN :startDate AND :endDate
            """)
    List<LeaveRequest> findApprovedInRange(@Param("employeeId") Long employeeId,
                                           @Param("leaveTypeId") Long leaveTypeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT lr.status AS status, COUNT(lr) AS count
            FROM LeaveRequest lr
            GROUP BY lr.status
            """)
    List<Object[]> countByStatus();

    Optional<LeaveRequest> findByIdAndStatus(Long id, LeaveRequestStatus status);
}
