package com.employee.employee_management.repository;

import com.employee.employee_management.entity.PerformanceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {

    List<PerformanceReview> findByEmployeeIdOrderByReviewDateDesc(Long employeeId);

    @Query("""
            SELECT pr FROM PerformanceReview pr
            WHERE (:employeeId IS NULL OR pr.employee.id = :employeeId)
              AND (:reviewerId IS NULL OR pr.reviewer.id = :reviewerId)
              AND (:rating IS NULL OR pr.rating = :rating)
            """)
    Page<PerformanceReview> search(@Param("employeeId") Long employeeId,
                                   @Param("reviewerId") Long reviewerId,
                                   @Param("rating") Integer rating,
                                   Pageable pageable);

    @Query("""
            SELECT pr.rating AS rating, COUNT(pr) AS count
            FROM PerformanceReview pr
            GROUP BY pr.rating
            ORDER BY pr.rating
            """)
    List<Object[]> countByRating();

    @Query("""
            SELECT pr FROM PerformanceReview pr
            ORDER BY pr.createdAt DESC
            """)
    List<PerformanceReview> findRecent(Pageable pageable);
}
