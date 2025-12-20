package com.verchuk.electro.config;

import com.verchuk.electro.model.Role;
import com.verchuk.electro.model.User;
import com.verchuk.electro.repository.RoleRepository;
import com.verchuk.electro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Создание ролей
        if (roleRepository.count() == 0) {
            roleRepository.save(Role.builder().name(Role.RoleName.DESIGNER).build());
            roleRepository.save(Role.builder().name(Role.RoleName.ADMIN).build());
        }

        // Создание администратора по умолчанию
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@electro.local")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("User")
                    .enabled(true)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            System.out.println("Default admin user created: username=admin, password=admin123");
        }
    }
}

