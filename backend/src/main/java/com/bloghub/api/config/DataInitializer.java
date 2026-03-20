package com.bloghub.api.config;

import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.repository.RoleRepository;
import com.bloghub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds the database with default roles and an admin user on first startup.
 * Only inserts records if they don't already exist, so this is idempotent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed roles
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(Role.RoleName.ROLE_USER).build()));

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(Role.RoleName.ROLE_ADMIN).build()));

        // Seed default admin user (change password in production!)
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .name("Administrator")
                    .username("admin")
                    .email("admin@bloghub.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created. CHANGE THE PASSWORD IN PRODUCTION!");
        }

        log.info("Database initialization complete.");
    }
}
