package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.dtos.ResponseObject;
import org.hospitalDonation.v1.dtos.SystemAdminRegistrationDTO;
import org.hospitalDonation.v1.entity.SystemAdmin;
import org.hospitalDonation.v1.service.SystemAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/system-admins")
@RequiredArgsConstructor
public class SystemAdminController {

    private final SystemAdminService systemAdminService;


    @GetMapping
    public ResponseEntity<ResponseObject> getAllSystemAdmin() {
        try {
            List<SystemAdmin> systemAdmins =   systemAdminService.getAllSystemAdmin();
            return new ResponseEntity<>(new ResponseObject(true, "get all System Admin successfully.", systemAdmins), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get all  System Admin: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> registerSystemAdmin(@RequestBody SystemAdminRegistrationDTO dto) {
        try {
            systemAdminService.registerSystemAdmin(dto);
            return new ResponseEntity<>(new ResponseObject(true, "System Admin registered successfully.", null), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to register System Admin: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseObject> getSystemAdminByUserId(@PathVariable Integer userId) {
        try {
            SystemAdmin admin = systemAdminService.getSystemAdminByUserId(userId);
            return new ResponseEntity<>(new ResponseObject(true, "Get System Admin by user ID successfully.", admin), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find System Admin with this user ID.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get System Admin by user ID", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}