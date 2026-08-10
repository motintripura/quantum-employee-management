package com.employee.employee_management.repository;

import com.employee.employee_management.entity.Attendance;
import com.employee.employee_management.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long employeeId, LocalDate from, LocalDate to);

    List<Attendance> findByAttendanceDateOrderByEmployeeIdAsc(LocalDate date);

    long countByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);

    long countByAttendanceDate(LocalDate date);

    @Query("""
            SELECT a FROM Attendance a
            WHERE (:employeeId IS NULL OR a.employee.id = :employeeId)
              AND (:status IS NULL OR a.status = :status)
              AND (:fromDate IS NULL OR a.attendanceDate >= :fromDate)
              AND (:toDate IS NULL OR a.attendanceDate <= :toDate)
            """)
    Page<Attendance> search(@Param("employeeId") Long employeeId,
                            @Param("status") AttendanceStatus status,
                            @Param("fromDate") LocalDate fromDate,
                            @Param("toDate") LocalDate toDate,
                            Pageable pageable);

    @Query("""
            SELECT a.status AS status, COUNT(a) AS count
            FROM Attendance a
            WHERE a.attendanceDate = :date
            GROUP BY a.status
            """)
    List<Object[]> countByStatusForDate(@Param("date") LocalDate date);
}
