package com.employee.employee_management.repository;

import com.employee.employee_management.dto.report.*;
import com.employee.employee_management.entity.AttendanceStatus;
import com.employee.employee_management.entity.LeaveRequestStatus;
import com.employee.employee_management.entity.Gender;
import com.employee.employee_management.entity.PaymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Repository
public class ReportRepository {

    @PersistenceContext
    private EntityManager em;

    private void setParam(Query query, String name, Object value) {
        if (value != null) {
            query.setParameter(name, value);
        }
    }

    // ========== EMPLOYEE REPORTS ==========

    public List<EmployeeReportDTO> getEmployeeReport(Long departmentId, Long designationId, Boolean status,
                                                      String gender, LocalDate fromDate, LocalDate toDate,
                                                      Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.EmployeeReportDTO(
                e.id, e.employeeCode, e.firstName, e.lastName, e.email, e.phone,
                e.gender, d.name, des.name, e.joiningDate, e.salary, e.status,
                NULL, e.address
            )
            FROM Employee e
            JOIN e.department d
            JOIN e.designation des
            WHERE 1=1
        """;

        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (designationId != null) jpql += " AND e.designation.id = :designationId";
        if (status != null) jpql += " AND e.status = :status";
        if (gender != null) jpql += " AND e.gender = :gender";
        if (fromDate != null) jpql += " AND e.joiningDate >= :fromDate";
        if (toDate != null) jpql += " AND e.joiningDate <= :toDate";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " ORDER BY e.firstName, e.lastName";

        Query query = em.createQuery(jpql, EmployeeReportDTO.class);
        setParam(query, "departmentId", departmentId);
        setParam(query, "designationId", designationId);
        setParam(query, "status", status);
        setParam(query, "gender", gender != null ? Gender.valueOf(gender) : null);
        setParam(query, "fromDate", fromDate);
        setParam(query, "toDate", toDate);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    public EmployeeStatisticsDTO getEmployeeStatistics(Long managerDepartmentId) {
        String deptFilter = managerDepartmentId != null ? " WHERE e.department.id = :deptId" : "";

        Query totalQuery = em.createQuery("SELECT COUNT(e) FROM Employee e" + deptFilter);
        setParam(totalQuery, "deptId", managerDepartmentId);
        long total = ((Long) totalQuery.getSingleResult());

        Query activeQuery = em.createQuery("SELECT COUNT(e) FROM Employee e WHERE e.status = true" +
                (managerDepartmentId != null ? " AND e.department.id = :deptId" : ""));
        setParam(activeQuery, "deptId", managerDepartmentId);
        long active = ((Long) activeQuery.getSingleResult());

        LocalDate startOfMonth = YearMonth.now().atDay(1);
        LocalDate endOfMonth = YearMonth.now().atEndOfMonth();

        Query newQuery = em.createQuery(
                "SELECT COUNT(e) FROM Employee e WHERE e.joiningDate >= :start AND e.joiningDate <= :end" +
                (managerDepartmentId != null ? " AND e.department.id = :deptId" : ""));
        newQuery.setParameter("start", startOfMonth);
        newQuery.setParameter("end", endOfMonth);
        setParam(newQuery, "deptId", managerDepartmentId);
        long newThisMonth = ((Long) newQuery.getSingleResult());

        Query byDeptQuery = em.createQuery(
                "SELECT d.name, COUNT(e) FROM Employee e JOIN e.department d" + deptFilter +
                " GROUP BY d.name ORDER BY d.name");
        setParam(byDeptQuery, "deptId", managerDepartmentId);
        List<Object[]> byDept = byDeptQuery.getResultList();

        Query byDesigQuery = em.createQuery(
                "SELECT des.name, COUNT(e) FROM Employee e JOIN e.designation des" + deptFilter +
                " GROUP BY des.name ORDER BY des.name");
        setParam(byDesigQuery, "deptId", managerDepartmentId);
        List<Object[]> byDesig = byDesigQuery.getResultList();

        return EmployeeStatisticsDTO.builder()
                .totalEmployees(total)
                .activeEmployees(active)
                .inactiveEmployees(total - active)
                .newEmployeesThisMonth(newThisMonth)
                .employeesByDepartment(byDept)
                .employeesByDesignation(byDesig)
                .build();
    }

    // ========== DEPARTMENT REPORTS ==========

    public List<DepartmentReportDTO> getDepartmentReport(Boolean status) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.DepartmentReportDTO(
                dep.id, dep.name, dep.description, dep.status,
                COUNT(e),
                COALESCE(SUM(e.salary), 0),
                SUM(CASE WHEN e.status = true THEN 1 ELSE 0 END),
                SUM(CASE WHEN e.status = false THEN 1 ELSE 0 END)
            )
            FROM Department dep
            LEFT JOIN Employee e ON e.department = dep
        """;

        if (status != null) {
            jpql += " WHERE dep.status = :status";
        }
        jpql += " GROUP BY dep.id, dep.name, dep.description, dep.status ORDER BY dep.name";

        Query query = em.createQuery(jpql, DepartmentReportDTO.class);
        setParam(query, "status", status);
        return query.getResultList();
    }

    // ========== DESIGNATION REPORTS ==========

    public List<DesignationReportDTO> getDesignationReport(Long departmentId, Boolean status) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.DesignationReportDTO(
                des.id, des.name, dep.name, des.status,
                COUNT(e),
                COALESCE(SUM(e.salary), 0)
            )
            FROM Designation des
            JOIN des.department dep
            LEFT JOIN Employee e ON e.designation = des
            WHERE 1=1
        """;

        if (departmentId != null) jpql += " AND dep.id = :departmentId";
        if (status != null) jpql += " AND des.status = :status";

        jpql += " GROUP BY des.id, des.name, dep.name, des.status ORDER BY dep.name, des.name";

        Query query = em.createQuery(jpql, DesignationReportDTO.class);
        setParam(query, "departmentId", departmentId);
        setParam(query, "status", status);
        return query.getResultList();
    }

    // ========== ATTENDANCE REPORTS ==========

    public List<AttendanceReportDTO> getAttendanceReport(Long employeeId, Long departmentId,
                                                          AttendanceStatus status, LocalDate fromDate,
                                                          LocalDate toDate, Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.AttendanceReportDTO(
                a.id, e.employeeCode,
                CONCAT(e.firstName, ' ', COALESCE(e.lastName, '')),
                d.name, des.name, a.attendanceDate,
                a.checkIn, a.checkOut, a.workingHours, a.overtime,
                a.status, a.remarks
            )
            FROM Attendance a
            JOIN a.employee e
            JOIN e.department d
            JOIN e.designation des
            WHERE 1=1
        """;

        if (employeeId != null) jpql += " AND e.id = :employeeId";
        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (status != null) jpql += " AND a.status = :status";
        if (fromDate != null) jpql += " AND a.attendanceDate >= :fromDate";
        if (toDate != null) jpql += " AND a.attendanceDate <= :toDate";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " ORDER BY e.firstName, e.lastName, a.attendanceDate DESC";

        Query query = em.createQuery(jpql, AttendanceReportDTO.class);
        setParam(query, "employeeId", employeeId);
        setParam(query, "departmentId", departmentId);
        setParam(query, "status", status);
        setParam(query, "fromDate", fromDate);
        setParam(query, "toDate", toDate);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    public List<MonthlyAttendanceReportDTO> getMonthlyAttendanceReport(Integer month, Integer year,
                                                                        Long departmentId, Long employeeId,
                                                                        Long managerDepartmentId) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        String jpql = """
            SELECT new com.employee.employee_management.dto.report.MonthlyAttendanceReportDTO(
                e.employeeCode,
                CONCAT(e.firstName, ' ', COALESCE(e.lastName, '')),
                d.name,
                COUNT(a),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.PRESENT THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.ABSENT THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.LATE THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.HALF_DAY THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.LEAVE THEN 1 ELSE 0 END),
                COALESCE(SUM(a.workingHours), 0),
                COALESCE(SUM(a.overtime), 0),
                0
            )
            FROM Attendance a
            JOIN a.employee e
            JOIN e.department d
            WHERE a.attendanceDate >= :monthStart AND a.attendanceDate <= :monthEnd
        """;

        if (employeeId != null) jpql += " AND e.id = :employeeId";
        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " GROUP BY e.id, e.employeeCode, e.firstName, e.lastName, d.name ORDER BY e.firstName, e.lastName";

        Query query = em.createQuery(jpql, MonthlyAttendanceReportDTO.class);
        query.setParameter("monthStart", monthStart);
        query.setParameter("monthEnd", monthEnd);
        setParam(query, "employeeId", employeeId);
        setParam(query, "departmentId", departmentId);
        setParam(query, "managerDeptId", managerDepartmentId);

        List<MonthlyAttendanceReportDTO> results = query.getResultList();

        for (MonthlyAttendanceReportDTO dto : results) {
            long workingDays = dto.getWorkingDays();
            BigDecimal pct = workingDays > 0
                    ? BigDecimal.valueOf(dto.getPresent())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(workingDays), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            dto.setAttendancePercentage(pct);
        }

        return results;
    }

    public AttendanceStatisticsDTO getAttendanceStatistics(Long employeeId, Long departmentId,
                                                           Integer month, Integer year,
                                                           Long managerDepartmentId) {
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        String jpql = """
            SELECT
                COUNT(a),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.PRESENT THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.ABSENT THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.LATE THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.HALF_DAY THEN 1 ELSE 0 END),
                SUM(CASE WHEN a.status = com.employee.employee_management.entity.AttendanceStatus.LEAVE THEN 1 ELSE 0 END),
                COALESCE(SUM(a.workingHours), 0),
                COALESCE(SUM(a.overtime), 0)
            FROM Attendance a
            JOIN a.employee e
            WHERE a.attendanceDate >= :monthStart AND a.attendanceDate <= :monthEnd
        """;

        if (employeeId != null) jpql += " AND e.id = :employeeId";
        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        Query query = em.createQuery(jpql);
        query.setParameter("monthStart", monthStart);
        query.setParameter("monthEnd", monthEnd);
        setParam(query, "employeeId", employeeId);
        setParam(query, "departmentId", departmentId);
        setParam(query, "managerDeptId", managerDepartmentId);

        Object[] row = (Object[]) query.getSingleResult();

        long total = row[0] != null ? ((Number) row[0]).longValue() : 0;
        long present = row[1] != null ? ((Number) row[1]).longValue() : 0;

        BigDecimal percentage = total > 0
                ? BigDecimal.valueOf(present).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AttendanceStatisticsDTO.builder()
                .totalWorkingDays(total)
                .present(present)
                .absent(row[2] != null ? ((Number) row[2]).longValue() : 0)
                .late(row[3] != null ? ((Number) row[3]).longValue() : 0)
                .halfDay(row[4] != null ? ((Number) row[4]).longValue() : 0)
                .leaveDays(row[5] != null ? ((Number) row[5]).longValue() : 0)
                .totalWorkingHours(row[6] != null ? (BigDecimal) row[6] : BigDecimal.ZERO)
                .totalOvertime(row[7] != null ? (BigDecimal) row[7] : BigDecimal.ZERO)
                .attendancePercentage(percentage)
                .build();
    }

    // ========== OVERTIME REPORTS ==========

    public List<OvertimeReportDTO> getOvertimeReport(Long departmentId, LocalDate fromDate,
                                                      LocalDate toDate, Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.OvertimeReportDTO(
                e.employeeCode,
                CONCAT(e.firstName, ' ', COALESCE(e.lastName, '')),
                d.name,
                a.attendanceDate,
                a.overtime
            )
            FROM Attendance a
            JOIN a.employee e
            JOIN e.department d
            WHERE a.overtime > 0
        """;

        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (fromDate != null) jpql += " AND a.attendanceDate >= :fromDate";
        if (toDate != null) jpql += " AND a.attendanceDate <= :toDate";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " ORDER BY e.firstName, e.lastName, a.attendanceDate DESC";

        Query query = em.createQuery(jpql, OvertimeReportDTO.class);
        setParam(query, "departmentId", departmentId);
        setParam(query, "fromDate", fromDate);
        setParam(query, "toDate", toDate);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    // ========== LEAVE REPORTS ==========

    public List<LeaveReportDTO> getLeaveReport(Long employeeId, Long departmentId, Long leaveTypeId,
                                                LeaveRequestStatus status, LocalDate fromDate,
                                                LocalDate toDate, Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.LeaveReportDTO(
                lr.id, e.employeeCode,
                CONCAT(e.firstName, ' ', COALESCE(e.lastName, '')),
                d.name, lt.name, lr.startDate, lr.endDate, lr.totalDays,
                lr.reason, lr.status,
                lr.approvedBy.username,
                lr.approvedAt
            )
            FROM LeaveRequest lr
            JOIN lr.employee e
            JOIN e.department d
            JOIN lr.leaveType lt
            LEFT JOIN lr.approvedBy
            WHERE 1=1
        """;

        if (employeeId != null) jpql += " AND e.id = :employeeId";
        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (leaveTypeId != null) jpql += " AND lr.leaveType.id = :leaveTypeId";
        if (status != null) jpql += " AND lr.status = :status";
        if (fromDate != null) jpql += " AND lr.startDate >= :fromDate";
        if (toDate != null) jpql += " AND lr.endDate <= :toDate";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " ORDER BY lr.startDate DESC, e.firstName, e.lastName";

        Query query = em.createQuery(jpql, LeaveReportDTO.class);
        setParam(query, "employeeId", employeeId);
        setParam(query, "departmentId", departmentId);
        setParam(query, "leaveTypeId", leaveTypeId);
        setParam(query, "status", status);
        setParam(query, "fromDate", fromDate);
        setParam(query, "toDate", toDate);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    public LeaveStatisticsDTO getLeaveStatistics(Long departmentId, Long managerDepartmentId) {
        String deptFilter = "";
        if (departmentId != null) deptFilter += " AND e.department.id = :departmentId";
        if (managerDepartmentId != null) deptFilter += " AND e.department.id = :managerDeptId";

        String jpql = """
            SELECT
                COUNT(lr),
                SUM(CASE WHEN lr.status = com.employee.employee_management.entity.LeaveRequestStatus.PENDING THEN 1 ELSE 0 END),
                SUM(CASE WHEN lr.status = com.employee.employee_management.entity.LeaveRequestStatus.APPROVED THEN 1 ELSE 0 END),
                SUM(CASE WHEN lr.status = com.employee.employee_management.entity.LeaveRequestStatus.REJECTED THEN 1 ELSE 0 END),
                SUM(CASE WHEN lr.status = com.employee.employee_management.entity.LeaveRequestStatus.CANCELLED THEN 1 ELSE 0 END),
                COALESCE(SUM(lr.totalDays), 0)
            FROM LeaveRequest lr
            JOIN lr.employee e
            WHERE 1=1
        """ + deptFilter;

        Query query = em.createQuery(jpql);
        setParam(query, "departmentId", departmentId);
        setParam(query, "managerDeptId", managerDepartmentId);

        Object[] row = (Object[]) query.getSingleResult();

        return LeaveStatisticsDTO.builder()
                .totalLeaveRequests(row[0] != null ? ((Number) row[0]).longValue() : 0)
                .pending(row[1] != null ? ((Number) row[1]).longValue() : 0)
                .approved(row[2] != null ? ((Number) row[2]).longValue() : 0)
                .rejected(row[3] != null ? ((Number) row[3]).longValue() : 0)
                .cancelled(row[4] != null ? ((Number) row[4]).longValue() : 0)
                .totalLeaveDays(row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO)
                .build();
    }

    public List<LeaveBalanceReportDTO> getLeaveBalanceReport(Long departmentId, Integer year,
                                                              Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.LeaveBalanceReportDTO(
                e.employeeCode,
                CONCAT(e.firstName, ' ', COALESCE(e.lastName, '')),
                d.name, lt.name, lb.year,
                lb.allocatedDays, lb.usedDays, lb.remainingDays
            )
            FROM LeaveBalance lb
            JOIN lb.employee e
            JOIN e.department d
            JOIN lb.leaveType lt
            WHERE lb.year = :year
        """;

        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " ORDER BY e.firstName, e.lastName, lt.name";

        Query query = em.createQuery(jpql, LeaveBalanceReportDTO.class);
        query.setParameter("year", year);
        setParam(query, "departmentId", departmentId);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    // ========== PAYROLL REPORTS ==========

    public List<PayrollReportDTO> getPayrollReport(Long employeeId, Long departmentId, Integer month,
                                                    Integer year, PaymentStatus paymentStatus,
                                                    Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.PayrollReportDTO(
                p.id, e.employeeCode,
                CONCAT(e.firstName, ' ', COALESCE(e.lastName, '')),
                d.name, des.name,
                p.month, p.year, p.basicSalary, p.houseAllowance,
                p.transportAllowance, p.medicalAllowance, p.overtime,
                p.bonus, p.tax, p.deduction, p.grossSalary, p.netSalary,
                p.paymentStatus, p.paymentDate
            )
            FROM Payroll p
            JOIN p.employee e
            JOIN e.department d
            JOIN e.designation des
            WHERE p.month = :month AND p.year = :year
        """;

        if (employeeId != null) jpql += " AND e.id = :employeeId";
        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (paymentStatus != null) jpql += " AND p.paymentStatus = :paymentStatus";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " ORDER BY e.firstName, e.lastName";

        Query query = em.createQuery(jpql, PayrollReportDTO.class);
        query.setParameter("month", month);
        query.setParameter("year", year);
        setParam(query, "employeeId", employeeId);
        setParam(query, "departmentId", departmentId);
        setParam(query, "paymentStatus", paymentStatus);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    public PayrollStatisticsDTO getPayrollStatistics(Integer month, Integer year, Long departmentId,
                                                      Long managerDepartmentId) {
        String deptFilter = "";
        if (departmentId != null) deptFilter += " AND e.department.id = :departmentId";
        if (managerDepartmentId != null) deptFilter += " AND e.department.id = :managerDeptId";

        String jpql = """
            SELECT
                COUNT(p),
                COALESCE(SUM(p.grossSalary), 0),
                COALESCE(SUM(p.netSalary), 0),
                COALESCE(SUM(p.tax), 0),
                COALESCE(SUM(p.deduction), 0),
                COALESCE(SUM(p.bonus), 0),
                COALESCE(SUM(p.overtime), 0),
                SUM(CASE WHEN p.paymentStatus = com.employee.employee_management.entity.PaymentStatus.PAID THEN 1 ELSE 0 END),
                SUM(CASE WHEN p.paymentStatus = com.employee.employee_management.entity.PaymentStatus.PENDING THEN 1 ELSE 0 END)
            FROM Payroll p
            JOIN p.employee e
            WHERE p.month = :month AND p.year = :year
        """ + deptFilter;

        Query query = em.createQuery(jpql);
        query.setParameter("month", month);
        query.setParameter("year", year);
        setParam(query, "departmentId", departmentId);
        setParam(query, "managerDeptId", managerDepartmentId);

        Object[] row = (Object[]) query.getSingleResult();

        return PayrollStatisticsDTO.builder()
                .totalPayroll(row[0] != null ? ((Number) row[0]).longValue() : 0)
                .totalGrossSalary(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                .totalNetSalary(row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO)
                .totalTax(row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO)
                .totalDeduction(row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO)
                .totalBonus(row[5] != null ? (BigDecimal) row[5] : BigDecimal.ZERO)
                .totalOvertime(row[6] != null ? (BigDecimal) row[6] : BigDecimal.ZERO)
                .paidPayroll(row[7] != null ? ((Number) row[7]).longValue() : 0)
                .pendingPayroll(row[8] != null ? ((Number) row[8]).longValue() : 0)
                .build();
    }

    public List<DepartmentPayrollReportDTO> getDepartmentPayrollReport(Integer month, Integer year,
                                                                        Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.DepartmentPayrollReportDTO(
                d.name,
                COUNT(DISTINCT e.id),
                COALESCE(SUM(p.basicSalary), 0),
                COALESCE(SUM(p.houseAllowance + p.transportAllowance + p.medicalAllowance), 0),
                COALESCE(SUM(p.overtime), 0),
                COALESCE(SUM(p.bonus), 0),
                COALESCE(SUM(p.deduction), 0),
                COALESCE(SUM(p.netSalary), 0)
            )
            FROM Payroll p
            JOIN p.employee e
            JOIN e.department d
            WHERE p.month = :month AND p.year = :year
        """;

        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " GROUP BY d.id, d.name ORDER BY d.name";

        Query query = em.createQuery(jpql, DepartmentPayrollReportDTO.class);
        query.setParameter("month", month);
        query.setParameter("year", year);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    public List<PaymentStatusReportDTO> getPaymentStatusReport(Integer month, Integer year,
                                                                Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.PaymentStatusReportDTO(
                p.paymentStatus,
                COUNT(p),
                COALESCE(SUM(p.netSalary), 0)
            )
            FROM Payroll p
            JOIN p.employee e
            WHERE p.month = :month AND p.year = :year
        """;

        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " GROUP BY p.paymentStatus ORDER BY p.paymentStatus";

        Query query = em.createQuery(jpql, PaymentStatusReportDTO.class);
        query.setParameter("month", month);
        query.setParameter("year", year);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    // ========== PERFORMANCE REPORTS ==========

    public List<PerformanceReportDTO> getPerformanceReport(Long employeeId, Long departmentId,
                                                            Integer rating, LocalDate fromDate,
                                                            LocalDate toDate, Long managerDepartmentId) {
        String jpql = """
            SELECT new com.employee.employee_management.dto.report.PerformanceReportDTO(
                pr.id, e.employeeCode,
                CONCAT(e.firstName, ' ', COALESCE(e.lastName, '')),
                d.name, des.name,
                CASE WHEN pr.reviewer IS NOT NULL
                    THEN CONCAT(pr.reviewer.firstName, ' ', COALESCE(pr.reviewer.lastName, ''))
                    ELSE NULL END,
                pr.reviewDate, pr.rating, pr.strengths, pr.weaknesses, pr.comments
            )
            FROM PerformanceReview pr
            JOIN pr.employee e
            JOIN e.department d
            JOIN e.designation des
            LEFT JOIN pr.reviewer
            WHERE 1=1
        """;

        if (employeeId != null) jpql += " AND e.id = :employeeId";
        if (departmentId != null) jpql += " AND e.department.id = :departmentId";
        if (rating != null) jpql += " AND pr.rating = :rating";
        if (fromDate != null) jpql += " AND pr.reviewDate >= :fromDate";
        if (toDate != null) jpql += " AND pr.reviewDate <= :toDate";
        if (managerDepartmentId != null) jpql += " AND e.department.id = :managerDeptId";

        jpql += " ORDER BY pr.reviewDate DESC, e.firstName, e.lastName";

        Query query = em.createQuery(jpql, PerformanceReportDTO.class);
        setParam(query, "employeeId", employeeId);
        setParam(query, "departmentId", departmentId);
        setParam(query, "rating", rating);
        setParam(query, "fromDate", fromDate);
        setParam(query, "toDate", toDate);
        setParam(query, "managerDeptId", managerDepartmentId);
        return query.getResultList();
    }

    public PerformanceStatisticsDTO getPerformanceStatistics(Long departmentId, Long managerDepartmentId) {
        String deptFilter = "";
        if (departmentId != null) deptFilter += " AND e.department.id = :departmentId";
        if (managerDepartmentId != null) deptFilter += " AND e.department.id = :managerDeptId";

        String baseWhere = "WHERE 1=1" + deptFilter;

        Query avgQuery = em.createQuery(
                "SELECT COALESCE(AVG(pr.rating), 0) FROM PerformanceReview pr JOIN pr.employee e " + baseWhere);
        setParam(avgQuery, "departmentId", departmentId);
        setParam(avgQuery, "managerDeptId", managerDepartmentId);
        BigDecimal avgRating = ((BigDecimal) avgQuery.getSingleResult());

        Query totalQuery = em.createQuery(
                "SELECT COUNT(pr) FROM PerformanceReview pr JOIN pr.employee e " + baseWhere);
        setParam(totalQuery, "departmentId", departmentId);
        setParam(totalQuery, "managerDeptId", managerDepartmentId);
        long totalReviews = ((Long) totalQuery.getSingleResult());

        String topFilter = deptFilter.isEmpty() ? "" : deptFilter;
        Query topQuery = em.createQuery(
                "SELECT COUNT(DISTINCT e.id) FROM PerformanceReview pr JOIN pr.employee e" +
                " WHERE pr.rating >= 4" + topFilter);
        setParam(topQuery, "departmentId", departmentId);
        setParam(topQuery, "managerDeptId", managerDepartmentId);
        long topPerformers = ((Long) topQuery.getSingleResult());

        Query lowQuery = em.createQuery(
                "SELECT COUNT(DISTINCT e.id) FROM PerformanceReview pr JOIN pr.employee e" +
                " WHERE pr.rating <= 2" + topFilter);
        setParam(lowQuery, "departmentId", departmentId);
        setParam(lowQuery, "managerDeptId", managerDepartmentId);
        long lowestPerformers = ((Long) lowQuery.getSingleResult());

        return PerformanceStatisticsDTO.builder()
                .averageRating(avgRating.setScale(1, RoundingMode.HALF_UP))
                .topPerformers(topPerformers)
                .lowestPerformers(lowestPerformers)
                .totalReviews(totalReviews)
                .build();
    }
}
