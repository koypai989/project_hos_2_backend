package org.hospitalDonation.v1.config;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.entity.SystemAdmin;
import org.hospitalDonation.v1.entity.User;
import org.hospitalDonation.v1.repository.SystemAdminRepository;
import org.hospitalDonation.v1.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final SystemAdminRepository systemAdminRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String adminEmail = "admin@admin.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            User adminUser = User.builder()
                    .email(adminEmail)
                    .password("admin123")
                    .role("ADMIN")
                    .build();

            User savedUser = userRepository.save(adminUser);
            SystemAdmin adminProfile = SystemAdmin.builder()
                    .fullName("คุณวันเสาร์")
                    .user(savedUser)
                    .build();

            systemAdminRepository.save(adminProfile);

            System.out.println(">> [Data Seeder] System Admin created successfully.");
        } else {
            System.out.println(">> [Data Seeder] Admin already exists, skipping...");
        }
    }
}