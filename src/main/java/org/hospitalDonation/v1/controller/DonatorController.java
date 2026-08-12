package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.dtos.ResponseObject;
import org.hospitalDonation.v1.dtos.DonatorRegistrationDTO;
import org.hospitalDonation.v1.entity.Donator;
import org.hospitalDonation.v1.service.DonatorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/donators")
@RequiredArgsConstructor
public class DonatorController {

    private final DonatorService donatorService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllDonators() {
        try {
            List<Donator> donators = donatorService.getAllDonators();
            return new ResponseEntity<>(new ResponseObject(true, "Get all donators successfully", donators), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get all donators", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{citizenId}")
    public ResponseEntity<ResponseObject> getDonatorByCitizenId(@PathVariable String citizenId) {
        try {
            Donator donator = donatorService.getDonatorByCitizenId(citizenId);
            return new ResponseEntity<>(new ResponseObject(true, "Get donator by citizen ID successfully.", donator), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find donator by provided citizen ID.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get donator by citizen ID", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PostMapping("/register")
    public ResponseEntity<ResponseObject> registerDonator(@RequestBody DonatorRegistrationDTO dto) {
        try {
            donatorService.registerDonator(dto);
            return new ResponseEntity<>(new ResponseObject(true, "Register donator successfully.", null), HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return new ResponseEntity<>(new ResponseObject(false, "Failed to register donator", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseObject> getDonatorByUserId(@PathVariable Integer userId) {
        try {
            Donator donator = donatorService.getDonatorByUserId(userId);
            return new ResponseEntity<>(new ResponseObject(true, "Get donator by user ID successfully.", donator), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find donator with this user ID.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get donator by user ID", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/user/{userId}")
    public ResponseEntity<ResponseObject> updateDonatorProfile(@PathVariable Integer userId, @RequestBody Donator updateData) {
        try {
            donatorService.updateDonator(userId, updateData);
            return ResponseEntity.ok(new ResponseObject(true, "Donator updated successfully", null));
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to update donator: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}