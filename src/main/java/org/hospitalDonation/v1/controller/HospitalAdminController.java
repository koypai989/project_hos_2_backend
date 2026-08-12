package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.dtos.ResponseObject;
import org.hospitalDonation.v1.dtos.CreateHospitalAdminDTO;
import org.hospitalDonation.v1.dtos.HospitalRegistrationDTO;
import org.hospitalDonation.v1.entity.HospitalAdmin;
import org.hospitalDonation.v1.service.HospitalAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/hospital-admins")
@RequiredArgsConstructor
public class HospitalAdminController {

    private final HospitalAdminService hospitalAdminService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllHospitalAdmins() {
        try {
            List<HospitalAdmin> admins = hospitalAdminService.getAllHospitalAdmins();
            return new ResponseEntity<>(new ResponseObject(true, "Get all hospital admins successfully", admins), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get all hospital admins", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getHospitalAdminById(@PathVariable Integer id) {
        try {
            HospitalAdmin admin = hospitalAdminService.getHospitalAdminById(id);
            return new ResponseEntity<>(new ResponseObject(true, "Get hospital admin by id successfully.", admin), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find hospital admin by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get hospital admin by id", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ResponseObject> saveHospitalAdmin(@RequestBody CreateHospitalAdminDTO createHospitalAdminDTO) {
        try {
            hospitalAdminService.createHospitalAdmin(createHospitalAdminDTO);
            return new ResponseEntity<>(new ResponseObject(true, "Create hospital admin successfully.", null), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to create hospital admin", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseObject> registerHospitalAndAdmin(@RequestBody HospitalRegistrationDTO dto) {
        try {
            hospitalAdminService.registerHospitalAndAdmin(dto);
            return new ResponseEntity<>(new ResponseObject(true, "Register hospital and admin successfully.", null), HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new ResponseEntity<>(new ResponseObject(false, "Failed to register hospital and admin", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseObject> getHospitalAdminByUserId(@PathVariable Integer userId) {
        try {
            HospitalAdmin admin = hospitalAdminService.getHospitalAdminByUserId(userId);
            return new ResponseEntity<>(new ResponseObject(true, "Get hospital admin by user ID successfully.", admin), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find hospital admin with this user ID.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get hospital admin by user ID", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}