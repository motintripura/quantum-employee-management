package com.employee.employee_management.security;

import com.employee.employee_management.entity.Role;
import com.employee.employee_management.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "c2VjdXJlLWVtcC1tYW5hZ2VtZW50LWp3dC1zZWNyZXQta2V5LTIwMjYtbXVzdC1iZS1sb25n";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 86400000L);
    }

    private User user() {
        return User.builder()
                .id(1L)
                .username("admin")
                .email("admin@ems.com")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
    }

    @Test
    void generateToken_extractUsername_roundTrips() {
        String token = jwtService.generateToken(user());

        assertNotNull(token);
        assertEquals("admin", jwtService.extractUsername(token));
        assertEquals(1L, jwtService.extractUserId(token));
    }

    @Test
    void isValid_matchingUser_returnsTrue() {
        String token = jwtService.generateToken(user());
        UserDetails details = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("pw")
                .authorities("ROLE_ADMIN")
                .build();

        assertTrue(jwtService.isValid(token, details));
    }

    @Test
    void isValid_wrongUser_returnsFalse() {
        String token = jwtService.generateToken(user());
        UserDetails other = org.springframework.security.core.userdetails.User.builder()
                .username("other")
                .password("pw")
                .authorities("ROLE_EMPLOYEE")
                .build();

        assertFalse(jwtService.isValid(token, other));
    }

    @Test
    void expiredToken_throws() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L);
        String token = shortLived.generateToken(user());
        Thread.sleep(10);

        UserDetails details = org.springframework.security.core.userdetails.User.builder()
                .username("admin").password("pw").authorities("ROLE_ADMIN").build();

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> shortLived.isValid(token, details));
    }

    @Test
    void getExpirationMs_returnsConfigured() {
        assertEquals(86400000L, jwtService.getExpirationMs());
    }

    @Test
    void tamperedToken_throws() {
        String token = jwtService.generateToken(user());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThrows(Exception.class, () -> jwtService.extractUsername(tampered));
    }
}