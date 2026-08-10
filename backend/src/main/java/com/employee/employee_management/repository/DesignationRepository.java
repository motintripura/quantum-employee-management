package com.employee.employee_management.repository;

import com.employee.employee_management.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DesignationRepository extends JpaRepository<Designation, Long> {

    boolean existsByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);

    boolean existsByNameIgnoreCaseAndDepartmentIdAndIdNot(String name, Long departmentId, Long id);

    boolean existsByDepartmentId(Long departmentId);

    List<Designation> findByDepartmentIdOrderByNameAsc(Long departmentId);

    List<Designation> findByDepartmentIdAndStatusTrueOrderByNameAsc(Long departmentId);

    List<Designation> findByStatusTrueOrderByNameAsc();

    @Query("""
            SELECT d FROM Designation d
            WHERE (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:departmentId IS NULL OR d.department.id = :departmentId)
              AND (:status IS NULL OR d.status = :status)
            """)
    Page<Designation> search(@Param("keyword") String keyword,
                             @Param("departmentId") Long departmentId,
                             @Param("status") Boolean status,
                             Pageable pageable);
}
