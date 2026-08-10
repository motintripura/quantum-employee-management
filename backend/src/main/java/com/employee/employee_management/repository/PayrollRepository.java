package com.employee.employee_management.repository;

import com.employee.employee_management.entity.Payroll;
import com.employee.employee_management.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    List<Payroll> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);

    @Query("""
            SELECT p FROM Payroll p
            WHERE (:employeeId IS NULL OR p.employee.id = :employeeId)
              AND (:month IS NULL OR p.month = :month)
              AND (:year IS NULL OR p.year = :year)
              AND (:paymentStatus IS NULL OR p.paymentStatus = :paymentStatus)
            """)
    Page<Payroll> search(@Param("employeeId") Long employeeId,
                         @Param("month") Integer month,
                         @Param("year") Integer year,
                         @Param("paymentStatus") PaymentStatus paymentStatus,
                         Pageable pageable);

    @Query("""
            SELECT p.month AS month, COALESCE(SUM(p.grossSalary), 0) AS total
            FROM Payroll p
            WHERE p.year = :year
            GROUP BY p.month
            ORDER BY p.month
            """)
    List<Object[]> sumGrossSalaryByMonth(@Param("year") Integer year);

    @Query("""
            SELECT COALESCE(SUM(p.netSalary), 0)
            FROM Payroll p
            WHERE p.month = :month AND p.year = :year
            """)
    BigDecimal sumNetSalaryByMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    long countByPaymentStatusAndMonthAndYear(PaymentStatus paymentStatus, Integer month, Integer year);
}
