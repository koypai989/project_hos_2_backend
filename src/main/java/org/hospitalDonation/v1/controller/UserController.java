package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.dtos.ResponseObject;
import org.hospitalDonation.v1.dtos.CreateUserDTO;
import org.hospitalDonation.v1.dtos.LoginUserDTO;
import org.hospitalDonation.v1.dtos.UpdateUserDTO;
import org.hospitalDonation.v1.dtos.UserResponseDTO;
import org.hospitalDonation.v1.entity.User;
import org.hospitalDonation.v1.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService   userService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllUsers(){
        try{
            List<UserResponseDTO> users = userService.getAllUser()
                    .stream().map(UserResponseDTO::from).toList();
            return new ResponseEntity<>(new ResponseObject(true,"Get all user successfully",users), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false,"Failed to get all user",null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getSaleHistoryById (@PathVariable Integer id) {
        try {
            User user = userService.getUserById(id);
            return new ResponseEntity<>(new ResponseObject(true, "Get User by id successfully.", UserResponseDTO.from(user)), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot found User by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get User by id", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateUser(@PathVariable Integer id, @RequestBody UpdateUserDTO dto) {
        try {
            userService.updateUser(id, dto);
            return new ResponseEntity<>(new ResponseObject(true, "Update user successfully.", null), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot found User by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to update user: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ResponseObject> saveUser(@RequestBody CreateUserDTO createUserDTO){
        try{
            userService.createUser(createUserDTO);
            return new ResponseEntity<>(new ResponseObject(true, "Create user successfully.", null), HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(new ResponseObject(false,"Failed to create user",null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseObject>   login(@RequestBody LoginUserDTO loginDTO){
        try{
            User loggedInUser = userService.loginUser(loginDTO);
            if (loggedInUser != null) {
                return new ResponseEntity<>(new ResponseObject(true, "User login successfully.", loggedInUser), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseObject(false, "Invalid email or password.", null), HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception e){
            return new ResponseEntity<>(new ResponseObject(false,"Failed to Login user",null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
