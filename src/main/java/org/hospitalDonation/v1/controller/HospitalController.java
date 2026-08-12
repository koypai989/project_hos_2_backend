package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.dtos.ResponseObject;
import org.hospitalDonation.v1.dtos.CreateHospitalDTO;
import org.hospitalDonation.v1.dtos.HospitalProfileUpdateDTO;
import org.hospitalDonation.v1.entity.Hospital;
import org.hospitalDonation.v1.entity.HospitalAdmin;
import org.hospitalDonation.v1.service.HospitalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllHospitals() {
        try {
            List<Hospital> hospitals = hospitalService.getAllHospitals();
            return new ResponseEntity<>(new ResponseObject(true, "Get all hospitals successfully", hospitals), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get all hospitals", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/search")
    public ResponseEntity<ResponseObject> searchHospitals(@RequestParam String keyword) {
        try {
            List<Hospital> hospitals = hospitalService.searchApprovedHospitals(keyword);
            return new ResponseEntity<>(
                    new ResponseObject(true, "Search successful", hospitals),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                    new ResponseObject(false, "Search failed: " + e.getMessage(), null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getHospitalById(@PathVariable Integer id) {
        try {
            Hospital hospital = hospitalService.getHospitalById(id);
            return new ResponseEntity<>(new ResponseObject(true, "Get hospital by id successfully.", hospital), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find hospital by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get hospital by id", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ResponseObject> saveHospital(@RequestBody CreateHospitalDTO createHospitalDTO) {
        try {
            hospitalService.createHospital(createHospitalDTO);
            return new ResponseEntity<>(new ResponseObject(true, "Create hospital successfully.", null), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to create hospital", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/pendings")
    public ResponseEntity<ResponseObject> getPendingHospitals() {
        try {
            List<Hospital> hospitals = hospitalService.getPendingHospitals();
            return new ResponseEntity<>(new ResponseObject(true, "Fetch hospital Pending successfully.", hospitals), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to fetch hospital requests: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/approved")
    public ResponseEntity<ResponseObject> getApproveHospitals() {
        try {
            List<Hospital> hospitals = hospitalService.getApproveHospitals();
            return new ResponseEntity<>(new ResponseObject(true, "Fetch hospital Approve successfully.", hospitals), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to fetch hospital requests: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<ResponseObject> approveHospitals(@PathVariable Integer id) {
        try {
            hospitalService.approveHospitalById(id);
            return new ResponseEntity<>(new ResponseObject(true, "Fetch hospital Approve successfully.", id), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find hospital by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to fetch hospital requests: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<ResponseObject> rejectHospitals(@PathVariable Integer id) {
        try {
            hospitalService.approveHospitalById(id);
            return new ResponseEntity<>(new ResponseObject(true, "hospital reject successfully.", null), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find hospital by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to reject hospital requests: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseObject> getHospitalProfileByUserId(@PathVariable Integer userId) {
        try {
            HospitalAdmin admin = hospitalService.getHospitalAdminByUserId(userId);
            Hospital hospital = hospitalService.getHospitalById(admin.getHospital().getHospitalId());
            HospitalProfileUpdateDTO profileDTO = new HospitalProfileUpdateDTO();

            profileDTO.setFirstName(admin.getFirstName());
            profileDTO.setLastName(admin.getLastName());
            profileDTO.setPhone(admin.getPhone());
            if (hospital != null) {
                profileDTO.setHospitalName(hospital.getHospitalName());
                profileDTO.setAddress(hospital.getAddress());
                profileDTO.setDescription(hospital.getDescription());
                profileDTO.setLogo(hospital.getLogo());
                profileDTO.setQrCode(hospital.getQrCode());
                profileDTO.setSignature(hospital.getSignature());
                profileDTO.setVerificationDoc(hospital.getVerificationDoc());
            }

            return ResponseEntity.ok(new ResponseObject(true, "Success", profileDTO));
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to load profile: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<ResponseObject> updateHospitalProfile(
            @PathVariable Integer userId,
            @RequestBody HospitalProfileUpdateDTO dto) {
        try {
            hospitalService.updateHospitalProfile(userId, dto);
            return ResponseEntity.ok(new ResponseObject(true, "Updated successfully", null));
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to update profile: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}