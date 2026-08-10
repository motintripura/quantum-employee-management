package com.employee.employee_management.service;

import com.employee.employee_management.dto.AuthResponse;
import com.employee.employee_management.dto.LoginRequest;
import com.employee.employee_management.dto.RegisterRequest;
import com.employee.employee_management.dto.UserResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    UserResponse register(RegisterRequest request);

    UserResponse getCurrentUser();
}
