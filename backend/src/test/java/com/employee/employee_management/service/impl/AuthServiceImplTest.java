package com.employee.employee_management.service.impl;

import com.employee.employee_management.dto.AuthResponse;
import com.employee.employee_management.dto.LoginRequest;
import com.employee.employee_management.dto.RegisterRequest;
import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import com.employee.employee_management.exception.DuplicateResourceException;
import com.employee.employee_management.exception.ResourceNotFoundException;
import com.employee.employee_management.repository.EmployeeRepository;
import com.employee.employee_management.repository.UserRepository;
import com.employee.employee_management.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_validCredentials_returnsTokenAndUser() {
        User user = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@ems.com")
                .password("encoded")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        when(userRepository.findByUsernameOrEmailIgnoreCase("admin", "admin"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        LoginRequest request = LoginRequest.builder().username("admin").password("Admin@123").build();
        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(Role.ADMIN, response.getUser().getRole());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_invalidCredentials_throwsBadCredentials() {
        doThrow(new BadCredentialsException("bad")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        LoginRequest request = LoginRequest.builder().username("admin").password("wrong").build();
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_unknownUser_throws() {
        when(userRepository.findByUsernameOrEmailIgnoreCase("ghost", "ghost"))
                .thenReturn(Optional.empty());

        LoginRequest request = LoginRequest.builder().username("ghost").password("whatever").build();
        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void register_duplicateUsername_throwsDuplicate() {
        when(userRepository.existsByUsername("hr1")).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .username("hr1")
                .email("hr1@ems.com")
                .password("password123")
                .role(Role.HR)
                .build();

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsDuplicate() {
        when(userRepository.existsByUsername("hr2")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("hr2@ems.com")).thenReturn(true);

        RegisterRequest request = RegisterRequest.builder()
                .username("hr2")
                .email("hr2@ems.com")
                .password("password123")
                .role(Role.HR)
                .build();

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void register_employeeNotFound_throwsNotFound() {
        when(userRepository.existsByUsername("hr3")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("hr3@ems.com")).thenReturn(false);
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        RegisterRequest request = RegisterRequest.builder()
                .username("hr3")
                .email("hr3@ems.com")
                .password("password123")
                .role(Role.HR)
                .employeeId(99L)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> authService.register(request));
    }

    @Test
    void register_validRequest_createsUser() {
        when(userRepository.existsByUsername("hr4")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("hr4@ems.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterRequest request = RegisterRequest.builder()
                .username("hr4")
                .email("hr4@ems.com")
                .password("password123")
                .role(Role.HR)
                .build();

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("hr4", response.getUsername());
        assertEquals(Role.HR, response.getRole());
        verify(userRepository).save(any(User.class));
    }
}