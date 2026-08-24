package com.tracker.auth.config;

import com.tracker.auth.model.Role;
import com.tracker.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            List<Role> roles = List.of(
                    Role.builder().name("USER").build(),
                    Role.builder().name("ADMIN").build()
            );
            roleRepository.saveAll(roles);
        }
    }
}