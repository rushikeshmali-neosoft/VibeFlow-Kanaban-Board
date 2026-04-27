package com.vibeflow.auth.security;

import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin123@gmail.com";
        if (userRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode("admin@12345"));
            admin.setRole("ROLE_ADMIN");
            admin.setIsActive(true);
            userRepository.save(admin);
            System.out.println("Default Admin User created successfully.");
        }
    }
}
