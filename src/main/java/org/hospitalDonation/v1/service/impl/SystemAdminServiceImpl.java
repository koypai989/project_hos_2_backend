package org.hospitalDonation.v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.SystemAdminRegistrationDTO;
import org.hospitalDonation.v1.entity.SystemAdmin;
import org.hospitalDonation.v1.entity.User;
import org.hospitalDonation.v1.repository.SystemAdminRepository;
import org.hospitalDonation.v1.repository.UserRepository;
import org.hospitalDonation.v1.service.PasswordService;
import org.hospitalDonation.v1.service.SystemAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemAdminServiceImpl implements SystemAdminService {

    private final SystemAdminRepository systemAdminRepository;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Override
    public List<SystemAdmin> getAllSystemAdmin() {
        return systemAdminRepository.findAll();
    }

    @Override
    @Transactional // ถ้าพัง มันจะยกเลิกการเซฟทั้งหมด (Rollback)
    public void registerSystemAdmin(SystemAdminRegistrationDTO dto) {
        String hashedPassword = passwordService.hashPassword(dto.getPassword());
        User user = User.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .role(dto.getRole() != null ? dto.getRole() : "ADMIN")
                .build();

        userRepository.save(user);


        SystemAdmin admin = SystemAdmin.builder()
                .fullName(dto.getFullName())
                .user(user)
                .build();

        systemAdminRepository.save(admin);
    }

    @Override
    public SystemAdmin getSystemAdminByUserId(Integer userId) {
        return systemAdminRepository.findByUser_UserId(userId).orElseThrow();
    }
}