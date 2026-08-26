package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.CreateUserDTO;
import org.hospitalDonation.v1.dtos.LoginUserDTO;
import org.hospitalDonation.v1.dtos.UpdateUserDTO;
import org.hospitalDonation.v1.entity.User;

import java.util.List;

public interface UserService {
    User getUserById(int id);
    List<User> getAllUser();
    void createUser(CreateUserDTO RreateUserDTO);
    User loginUser(LoginUserDTO loginUserDTO);
    void updateUser(int id, UpdateUserDTO updateUserDTO);
}
