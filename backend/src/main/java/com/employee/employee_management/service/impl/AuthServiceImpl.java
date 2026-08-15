package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.AuthResponse;
import com.employee.employee_management.dto.EmployeeResponse;
import com.employee.employee_management.dto.LoginRequest;
import com.employee.employee_management.dto.RegisterRequest;
import com.employee.employee_management.dto.UserResponse;
import com.employee.employee_management.entity.Employee;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.exception.UnauthorizedException;
import com.employee.employee_management.mapper.EmployeeMapper;
import com.employee.employee_management.mapper.UserMapper;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.security.JwtService;
import com.employee.employee_management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsernameOrEmailIgnoreCase(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        String token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .user(UserMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with id: " + request.getEmployeeId()));
            if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
                throw new DuplicateResourceException("A user is already linked to this employee");
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .employee(employee)
                .enabled(true)
                .build();
        userRepository.save(user);
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return UserMapper.toResponse(getCurrentUserEntity());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getMyProfile() {
        User user = getCurrentUserEntity();
        Employee employee = user.getEmployee();
        if (employee == null) {
            throw new ResourceNotFoundException("No employee profile is linked to this user");
        }
        return EmployeeMapper.toResponse(employee);
    }

    private User getCurrentUserEntity() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
