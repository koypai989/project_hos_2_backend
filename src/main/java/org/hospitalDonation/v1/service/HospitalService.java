package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.CreateHospitalDTO;
import org.hospitalDonation.v1.dtos.HospitalProfileUpdateDTO;
import org.hospitalDonation.v1.entity.Hospital;
import org.hospitalDonation.v1.entity.HospitalAdmin;

import java.util.List;

public interface HospitalService {
    Hospital getHospitalById(int id);
    List<Hospital> getAllHospitals();
    void createHospital(CreateHospitalDTO createHospitalDTO);
    List<Hospital> getPendingHospitals();
    List<Hospital> getApproveHospitals();
    void approveHospitalById(int id);
    void rejectHospitalById(int id);
    List<Hospital> searchApprovedHospitals(String keyword);
    HospitalAdmin getHospitalAdminByUserId(Integer userId);
    void updateHospitalProfile(Integer userId, HospitalProfileUpdateDTO dto);
}