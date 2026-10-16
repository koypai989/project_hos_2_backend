package org.hospitalDonation.v1.config;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.SystemAdminRegistrationDTO;
import org.hospitalDonation.v1.repository.UserRepository;
import org.hospitalDonation.v1.service.SystemAdminService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// สร้างบัญชีผู้ดูแลระบบกลางเริ่มต้น เมื่อยังไม่มีในฐานข้อมูล (เช่น ใช้ DB ใหม่)
// สมัครผ่าน SystemAdminService เพื่อให้รหัสผ่านถูกเข้ารหัส (BCrypt) เหมือนการสมัครปกติ จึงล็อกอินได้ทันที
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final SystemAdminService systemAdminService;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@admin.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            SystemAdminRegistrationDTO dto = new SystemAdminRegistrationDTO(
                    adminEmail, "admin123", "ADMIN", "คุณวันเสาร์");
            systemAdminService.registerSystemAdmin(dto);

            System.out.println(">> [Data Seeder] System Admin created successfully.");
        } else {
            System.out.println(">> [Data Seeder] Admin already exists, skipping...");
        }
    }
}
