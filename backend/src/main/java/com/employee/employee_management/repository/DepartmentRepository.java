package com.employee.employee_management.repository;

import com.employee.employee_management.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Department> findAllByOrderByNameAsc();

    @Query("""
            SELECT d FROM Department d
            WHERE (:keyword IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR d.status = :status)
            """)
    Page<Department> search(@Param("keyword") String keyword,
                            @Param("status") Boolean status,
                            Pageable pageable);
}
