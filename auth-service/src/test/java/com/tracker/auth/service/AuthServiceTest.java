package com.tracker.auth.service;

import com.tracker.auth.dto.AuthResponse;
import com.tracker.auth.dto.RegisterRequest;
import com.tracker.auth.kafka.UserRegisteredProducer;
import com.tracker.auth.model.Role;
import com.tracker.auth.model.User;
import com.tracker.auth.repository.RoleRepository;
import com.tracker.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRegisteredProducer userRegisteredProducer;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldSaveUser_WhenValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");
        request.setEmail("test@example.com");
        request.setFirstName("Test");
        request.setLastName("User");

        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setRole(userRole);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("testpass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.register(request);

        verify(userRepository).save(any(User.class));
        verify(userRegisteredProducer).sendUserRegisteredEvent(any());
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        String username = "testuser";
        String password = "testpass";

        Role role = new Role();
        role.setName("USER");

        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPass");
        user.setRole(role);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, "encodedPass")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(username, password);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo(username);
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("unknown", "pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}