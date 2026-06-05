package com.bloghub.api.config;

import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.repository.RoleRepository;
import com.bloghub.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean bootstrapAdminEnabled;

    @Value("${app.bootstrap.admin.username:admin}")
    private String bootstrapAdminUsername;

    @Value("${app.bootstrap.admin.email:admin@bloghub.local}")
    private String bootstrapAdminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String bootstrapAdminPassword;

    @Override
    public void run(String... args) {
        // Seed roles
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(Role.RoleName.ROLE_USER).build()));

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(Role.RoleName.ROLE_ADMIN).build()));

        if (!bootstrapAdminEnabled) {
            log.info("Admin bootstrap disabled. Set APP_BOOTSTRAP_ADMIN_ENABLED=true and APP_BOOTSTRAP_ADMIN_PASSWORD to seed one.");
            log.info("Database initialization complete.");
            return;
        }

        if (!StringUtils.hasText(bootstrapAdminPassword)) {
            log.warn("Admin bootstrap requested but no password was supplied. Skipping admin user creation.");
            log.info("Database initialization complete.");
            return;
        }

        if (!userRepository.existsByUsername(bootstrapAdminUsername)) {
            User admin = User.builder()
                    .name("Administrator")
                    .username(bootstrapAdminUsername)
                    .email(bootstrapAdminEmail)
                    .password(passwordEncoder.encode(bootstrapAdminPassword))
                    .emailVerified(true)
                    .roles(Set.of(adminRole, userRole))
                    .build();
            userRepository.save(admin);
            log.info("Bootstrap admin user created: {}", bootstrapAdminUsername);
        }

        log.info("Database initialization complete.");
    }
}
