package com.tracker.auth.service;

import com.tracker.auth.model.Role;
import com.tracker.auth.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Устанавливаем значения через Reflection (т.к. @Value не работает в тесте)
        ReflectionTestUtils.setField(jwtService, "secret",
                "a7f9e3d8c2b1f6e9d4c7a2b5f8e1d3c9a7f4e2b8c6d0f5e9a3c7b1d4f8e2a5c9");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);

        Role role = new Role();
        role.setName("USER");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(role);

        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractClaims_ShouldReturnClaims_WhenTokenValid() {
        ReflectionTestUtils.setField(jwtService, "secret",
                "a7f9e3d8c2b1f6e9d4c7a2b5f8e1d3c9a7f4e2b8c6d0f5e9a3c7b1d4f8e2a5c9");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);

        Role role = new Role();
        role.setName("USER");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(role);

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.extractClaims(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("role")).isEqualTo("USER");
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenValid() {
        ReflectionTestUtils.setField(jwtService, "secret",
                "a7f9e3d8c2b1f6e9d4c7a2b5f8e1d3c9a7f4e2b8c6d0f5e9a3c7b1d4f8e2a5c9");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);

        Role role = new Role();
        role.setName("USER");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(role);

        String token = jwtService.generateToken(user);

        boolean valid = jwtService.isTokenValid(token);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenInvalid() {
        ReflectionTestUtils.setField(jwtService, "secret",
                "a7f9e3d8c2b1f6e9d4c7a2b5f8e1d3c9a7f4e2b8c6d0f5e9a3c7b1d4f8e2a5c9");

        boolean valid = jwtService.isTokenValid("invalid.token.here");

        assertThat(valid).isFalse();
    }
}