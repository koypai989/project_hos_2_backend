package org.hospitalDonation.v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.CreateUserDTO;
import org.hospitalDonation.v1.dtos.LoginUserDTO;
import org.hospitalDonation.v1.dtos.UpdateUserDTO;
import org.hospitalDonation.v1.entity.User;
import org.hospitalDonation.v1.repository.UserRepository;
import org.hospitalDonation.v1.service.PasswordService;
import org.hospitalDonation.v1.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordService passwordService;

    @Override
    public User getUserById(int id) {
        return userRepository.getReferenceById(id);
    }

    @Override
    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public void createUser(CreateUserDTO createUserDTO) {
        String hashedPassword = passwordService.hashPassword(createUserDTO.getPassword());
        User toSaveToDo = User.builder()
                .email(createUserDTO.getEmail())
                .password(hashedPassword)
                .role(createUserDTO.getRole())
                .build();
        userRepository.save(toSaveToDo);
    }

    @Override
    @Transactional
    public void updateUser(int id, UpdateUserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) user.setEmail(dto.getEmail());
        if (dto.getRole() != null && !dto.getRole().isBlank()) user.setRole(dto.getRole());
        // ถ้าส่ง password มา = ตั้งรหัสผ่านใหม่ (hash ก่อนเก็บ)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordService.hashPassword(dto.getPassword()));
        }
        userRepository.save(user);
    }

    @Override
    public User loginUser(LoginUserDTO loginUserDTO) {
        var userOptional = userRepository.findByEmail(loginUserDTO.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            boolean isComplete = passwordService.verifyPassword(loginUserDTO.getPassword(),user.getPassword());
            if (isComplete) {
                return user;
            }
        }
        return null;
    }
}
