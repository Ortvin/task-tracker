package com.tracker.auth.service;

import com.tracker.auth.dto.AuthResponse;
import com.tracker.auth.dto.RegisterRequest;
import com.tracker.auth.event.UserRegisteredEvent;
import com.tracker.auth.kafka.UserRegisteredProducer;
import com.tracker.auth.model.Role;
import com.tracker.auth.model.User;
import com.tracker.auth.repository.RoleRepository;
import com.tracker.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRegisteredProducer userRegisteredProducer;

    public void register(RegisterRequest request) {
        // Проверка, что пользователь не существует
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Получаем роль USER по умолчанию
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        // Создаём пользователя
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .role(userRole)
                .build();

        userRepository.save(user);

        User savedUser = userRepository.save(user);

        // Отправляем событие
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .middleName(savedUser.getMiddleName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().getName())
                .build();

        userRegisteredProducer.sendUserRegisteredEvent(event);
    }

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().getName())
                .expiresIn(jwtService.getExpirationInSeconds())
                .build();
    }
}