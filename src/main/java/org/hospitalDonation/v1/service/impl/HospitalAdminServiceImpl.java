package org.hospitalDonation.v1.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.CreateHospitalAdminDTO;
import org.hospitalDonation.v1.dtos.HospitalRegistrationDTO;
import org.hospitalDonation.v1.entity.HospitalAdmin;
import org.hospitalDonation.v1.entity.Hospital;
import org.hospitalDonation.v1.entity.User;
import org.hospitalDonation.v1.repository.HospitalAdminRepository;
import org.hospitalDonation.v1.repository.HospitalRepository;
import org.hospitalDonation.v1.repository.UserRepository;
import org.hospitalDonation.v1.service.HospitalAdminService;
import org.hospitalDonation.v1.service.PasswordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalAdminServiceImpl implements HospitalAdminService {

    private final HospitalAdminRepository hospitalAdminRepository;
    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordService passwordService;

    @Override
    public HospitalAdmin getHospitalAdminById(int id) {
        return hospitalAdminRepository.getReferenceById(id);
    }

    @Override
    public List<HospitalAdmin> getAllHospitalAdmins() {
        return hospitalAdminRepository.findAll();
    }

    @Override
    public void createHospitalAdmin(CreateHospitalAdminDTO dto) {
        HospitalAdmin admin = HospitalAdmin.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .build();

        if (dto.getUserId() != null) {
            User user = userRepository.getReferenceById(dto.getUserId());
            admin.setUser(user);
        }

        if (dto.getHospitalId() != null) {
            Hospital hospital = hospitalRepository.getReferenceById(dto.getHospitalId());
            admin.setHospital(hospital);
        }

        hospitalAdminRepository.save(admin);
    }

    @Transactional
    @Override
    public void registerHospitalAndAdmin(HospitalRegistrationDTO dto) {

        String hashedPassword = passwordService.hashPassword(dto.getPassword());
        User user = User.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .role(dto.getRole())
                .build();
        user = userRepository.save(user);

        Hospital hospital = Hospital.builder()
                .hospitalName(dto.getHospitalName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .logo(dto.getLogo())
                .qrCode(dto.getQrCode())
                .signature(dto.getSignature())
                .signatureName(dto.getSignatureName())
                .verificationStatus(dto.getVerificationStatus())
                .verificationDoc(dto.getVerificationDoc())
                .build();
        hospital = hospitalRepository.save(hospital);

        HospitalAdmin admin = HospitalAdmin.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .user(user)
                .hospital(hospital)
                .build();
        hospitalAdminRepository.save(admin);
    }

    @Override
    public HospitalAdmin getHospitalAdminByUserId(Integer userId) {
        return hospitalAdminRepository.findByUser_UserId(userId).orElseThrow();
    }
}