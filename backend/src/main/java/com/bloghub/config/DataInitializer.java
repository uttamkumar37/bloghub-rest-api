package com.bloghub.config;

import com.bloghub.domain.Role;
import com.bloghub.domain.RoleName;
import com.bloghub.domain.User;
import com.bloghub.repository.RoleRepository;
import com.bloghub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roles, UserRepository users, PasswordEncoder passwordEncoder) {
        this.roles = roles;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roles.findByName(RoleName.ROLE_ADMIN).orElseGet(() -> {
            Role r = new Role();
            r.setName(RoleName.ROLE_ADMIN);
            return roles.save(r);
        });
        Role userRole = roles.findByName(RoleName.ROLE_USER).orElseGet(() -> {
            Role r = new Role();
            r.setName(RoleName.ROLE_USER);
            return roles.save(r);
        });

        if (!users.existsByEmailIgnoreCase("admin@bloghub.local")) {
            User admin = new User();
            admin.setEmail("admin@bloghub.local");
            admin.setDisplayName("Admin");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(adminRole);
            users.save(admin);
            log.info("Created default admin user admin@bloghub.local / Admin@123");
        }
    }
}

