package com.employee.employee_management.repository;

import com.employee.employee_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameOrEmailIgnoreCase(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmployeeId(Long employeeId);

    Optional<User> findByEmployeeId(Long employeeId);
}
