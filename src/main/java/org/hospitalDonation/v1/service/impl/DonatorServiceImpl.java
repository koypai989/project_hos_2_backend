package org.hospitalDonation.v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.DonatorRegistrationDTO;
import org.hospitalDonation.v1.entity.Donator;
import org.hospitalDonation.v1.entity.User;
import org.hospitalDonation.v1.repository.DonatorRepository;
import org.hospitalDonation.v1.repository.UserRepository;
import org.hospitalDonation.v1.service.DonatorService;
import org.hospitalDonation.v1.service.PasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonatorServiceImpl implements DonatorService {

    private final DonatorRepository donatorRepository;
    private final UserRepository userRepository;
    private final PasswordService passwordService;

    @Override
    public Donator getDonatorByCitizenId(String citizenId) {
        // ใช้ findById.orElse(null) หรือ orElseThrow() จะปลอดภัยกว่า getReferenceById สำหรับ String ID ครับ
        return donatorRepository.findById(citizenId).orElseThrow();
    }

    @Override
    public List<Donator> getAllDonators() {
        return donatorRepository.findAll();
    }

    @Transactional
    @Override
    public void registerDonator(DonatorRegistrationDTO dto) {
        String hashedPassword = passwordService.hashPassword(dto.getPassword());
        User user = User.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .role(dto.getRole())
                .build();
        user = userRepository.save(user);

        Donator donator = Donator.builder()
                .citizenID(dto.getCitizenID())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .dateOfBirth(dto.getDateOfBirth())
                .address(dto.getAddress())
                .user(user)
                .build();

        donatorRepository.save(donator);
    }

    @Override
    public Donator getDonatorByUserId(Integer userId) {
        return donatorRepository.findByUser_UserId(userId).orElseThrow();
    }
    @Override
    @Transactional
    public void updateDonator(Integer userId, Donator updateData) {

        Donator existingDonator = getDonatorByUserId(userId);

        if (updateData.getFirstName() != null) {
            existingDonator.setFirstName(updateData.getFirstName());
        }
        if (updateData.getLastName() != null) {
            existingDonator.setLastName(updateData.getLastName());
        }
        if (updateData.getCitizenID() != null) {
            existingDonator.setCitizenID(updateData.getCitizenID());
        }
        if (updateData.getDateOfBirth() != null) {
            existingDonator.setDateOfBirth(updateData.getDateOfBirth());
        }
        if (updateData.getAddress() != null) {
            existingDonator.setAddress(updateData.getAddress());
        }

        donatorRepository.save(existingDonator);
    }
}