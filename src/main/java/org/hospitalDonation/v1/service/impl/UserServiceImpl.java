package org.hospitalDonation.v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.CreateUserDTO;
import org.hospitalDonation.v1.dtos.LoginUserDTO;
import org.hospitalDonation.v1.entity.User;
import org.hospitalDonation.v1.repository.UserRepository;
import org.hospitalDonation.v1.service.PasswordService;
import org.hospitalDonation.v1.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

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
