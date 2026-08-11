package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.PerformanceRequest;
import com.employee.employee_management.dto.PerformanceResponse;
import com.employee.employee_management.dto.StatusCountResponse;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.PerformanceReview;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.ForbiddenException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.mapper.PerformanceMapper;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.PerformanceReviewRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.service.PerformanceService;
import com.employee.employee_management.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceReviewRepository performanceReviewRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PerformanceResponse> getAll(Long employeeId, Long reviewerId, Integer rating, Pageable pageable) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE) {
            Long ownId = user.getEmployee().getId();
            if (employeeId != null && !employeeId.equals(ownId)) {
                throw new ForbiddenException("Employees can only view their own performance reviews");
            }
            employeeId = ownId;
        }
        Page<PerformanceReview> page = performanceReviewRepository.search(employeeId, reviewerId, rating, pageable);
        return PageResponse.from(page.map(PerformanceMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PerformanceResponse> getEmployeePerformance(Long employeeId) {
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !employeeId.equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only view their own performance reviews");
        }
        findEmployee(employeeId);
        return performanceReviewRepository.findByEmployeeIdOrderByReviewDateDesc(employeeId)
                .stream().map(PerformanceMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceResponse getById(Long id) {
        PerformanceReview review = findReview(id);
        User user = getCurrentUser();
        if (user.getRole() == Role.EMPLOYEE && !review.getEmployee().getId().equals(user.getEmployee().getId())) {
            throw new ForbiddenException("Employees can only view their own performance reviews");
        }
        return PerformanceMapper.toResponse(review);
    }

    @Override
    @Transactional
    public PerformanceResponse create(PerformanceRequest request) {
        Employee employee = findEmployee(request.getEmployeeId());
        User reviewer = resolveReviewer(request.getReviewerId());

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .reviewer(reviewer)
                .reviewDate(request.getReviewDate())
                .rating(request.getRating())
                .strengths(request.getStrengths())
                .weaknesses(request.getWeaknesses())
                .comments(request.getComments())
                .build();
        performanceReviewRepository.save(review);
        return PerformanceMapper.toResponse(review);
    }

    @Override
    @Transactional
    public PerformanceResponse update(Long id, PerformanceRequest request) {
        PerformanceReview review = findReview(id);
        Employee employee = findEmployee(request.getEmployeeId());
        User reviewer = resolveReviewer(request.getReviewerId());

        review.setEmployee(employee);
        review.setReviewer(reviewer);
        review.setReviewDate(request.getReviewDate());
        review.setRating(request.getRating());
        review.setStrengths(request.getStrengths());
        review.setWeaknesses(request.getWeaknesses());
        review.setComments(request.getComments());
        performanceReviewRepository.save(review);
        return PerformanceMapper.toResponse(review);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PerformanceReview review = findReview(id);
        performanceReviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatusCountResponse> ratingReport() {
        List<StatusCountResponse> result = new ArrayList<>();
        for (Object[] row : performanceReviewRepository.countByRating()) {
            result.add(StatusCountResponse.builder()
                    .name("rating_" + row[0])
                    .count((Long) row[1])
                    .build());
        }
        return result;
    }

    private User resolveReviewer(Long reviewerId) {
        if (reviewerId == null) {
            return getCurrentUser();
        }
        return userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer user not found with id: " + reviewerId));
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

    private PerformanceReview findReview(Long id) {
        return performanceReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found with id: " + id));
    }
}
