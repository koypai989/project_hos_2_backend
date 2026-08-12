package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.dtos.ResponseObject;
import org.hospitalDonation.v1.dtos.CreateFoundationDTO;
import org.hospitalDonation.v1.dtos.UpdateFoundationDTO;
import org.hospitalDonation.v1.entity.Foundation;
import org.hospitalDonation.v1.service.FoundationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/foundations")
@RequiredArgsConstructor
public class FoundationController {

    private final FoundationService foundationService;

    @GetMapping
    public ResponseEntity<ResponseObject> getAllFoundations() {
        try {
            List<Foundation> foundations = foundationService.getAllFoundations();
            return new ResponseEntity<>(new ResponseObject(true, "Get all foundations successfully", foundations), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get all foundations", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getFoundationById(@PathVariable Integer id) {
        try {
            Foundation foundation = foundationService.getFoundationById(id);
            return new ResponseEntity<>(new ResponseObject(true, "Get foundation by id successfully.", foundation), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find foundation by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get foundation by id", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<ResponseObject> getFoundationsByHospitalId(@PathVariable Integer hospitalId) {
        try {
            List<Foundation> foundations = foundationService.getFoundationsByHospitalId(hospitalId);
            return new ResponseEntity<>(new ResponseObject(true, "Get foundations by hospital successfully.", foundations), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to get foundations by hospital: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<ResponseObject> saveFoundation(@RequestBody CreateFoundationDTO createFoundationDTO) {
        try {
            Foundation foundation = foundationService.createFoundation(createFoundationDTO);
            return new ResponseEntity<>(new ResponseObject(true, "Create foundation successfully.", foundation), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed to create foundation: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateFoundation(@PathVariable Integer id,
                                                           @RequestBody UpdateFoundationDTO updateFoundationDTO) {
        try {
            Foundation foundation = foundationService.updateFoundation(id, updateFoundationDTO);
            return new ResponseEntity<>(new ResponseObject(true, "Update foundation successfully.", foundation), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find foundation by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to update foundation: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteFoundation(@PathVariable Integer id) {
        try {
            foundationService.deleteFoundation(id);
            return new ResponseEntity<>(new ResponseObject(true, "Delete foundation successfully.", null), HttpStatus.OK);
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                return new ResponseEntity<>(new ResponseObject(false, "Cannot find foundation by provided id.", null), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(new ResponseObject(false, "Failed to delete foundation: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
