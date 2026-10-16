package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.dtos.ResponseObject;
import org.hospitalDonation.v1.dtos.CreateDonationDTO;
import org.hospitalDonation.v1.dtos.DonationResponseDTO;
import org.hospitalDonation.v1.entity.Donation;
import org.hospitalDonation.v1.service.DonationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    public ResponseEntity<ResponseObject> saveDonation(@RequestBody CreateDonationDTO dto) {
        try {
            Donation donation = donationService.createDonation(dto);
            DonationResponseDTO result = DonationResponseDTO.from(donation);
            return new ResponseEntity<>(new ResponseObject(true, "บันทึกการบริจาคสำเร็จ", result), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            // ตรวจสลิปไม่ผ่าน / ยอดไม่ตรง / สลิปซ้ำ → 400 พร้อมข้อความ
            return new ResponseEntity<>(new ResponseObject(false, e.getMessage(), null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "บันทึกการบริจาคไม่สำเร็จ: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<ResponseObject> getByHospital(@PathVariable Integer hospitalId) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByHospitalId(hospitalId)
                    .stream().map(DonationResponseDTO::from).toList();
            return new ResponseEntity<>(new ResponseObject(true, "Get donations by hospital successfully.", donations), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/foundation/{foundationId}")
    public ResponseEntity<ResponseObject> getByFoundation(@PathVariable Integer foundationId) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByFoundationId(foundationId)
                    .stream().map(DonationResponseDTO::from).toList();
            return new ResponseEntity<>(new ResponseObject(true, "Get donations by foundation successfully.", donations), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/donator/{citizenId}")
    public ResponseEntity<ResponseObject> getByCitizen(@PathVariable String citizenId) {
        try {
            List<DonationResponseDTO> donations = donationService.getDonationsByCitizenId(citizenId)
                    .stream().map(DonationResponseDTO::from).toList();
            return new ResponseEntity<>(new ResponseObject(true, "Get donations by donator successfully.", donations), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Failed: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
