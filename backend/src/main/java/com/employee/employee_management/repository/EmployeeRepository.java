package com.employee.employee_management.repository;

import com.employee.employee_management.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    Optional<Employee> findByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndIdNot(String employeeCode, Long id);

    long countByStatusTrue();

    long countByStatusFalse();

    long countByDepartmentId(Long departmentId);

    long countByDesignationId(Long designationId);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:keyword IS NULL
                   OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:departmentId IS NULL OR e.department.id = :departmentId)
              AND (:designationId IS NULL OR e.designation.id = :designationId)
              AND (:status IS NULL OR e.status = :status)
            """)
    Page<Employee> search(@Param("keyword") String keyword,
                          @Param("departmentId") Long departmentId,
                          @Param("designationId") Long designationId,
                          @Param("status") Boolean status,
                          Pageable pageable);

    @Query("""
            SELECT d.name AS name, COUNT(e.id) AS count
            FROM Employee e JOIN e.department d
            GROUP BY d.name, d.id
            ORDER BY d.name
            """)
    List<Object[]> countByDepartment();
}
