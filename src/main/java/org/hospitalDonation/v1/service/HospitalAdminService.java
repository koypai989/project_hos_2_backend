package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.CreateHospitalAdminDTO;
import org.hospitalDonation.v1.dtos.HospitalRegistrationDTO;
import org.hospitalDonation.v1.entity.HospitalAdmin;

import java.util.List;

public interface HospitalAdminService {
    HospitalAdmin getHospitalAdminById(int id);
    List<HospitalAdmin> getAllHospitalAdmins();
    void createHospitalAdmin(CreateHospitalAdminDTO createHospitalAdminDTO);
    void registerHospitalAndAdmin(HospitalRegistrationDTO dto);
    HospitalAdmin getHospitalAdminByUserId(Integer userId);
}