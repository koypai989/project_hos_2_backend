package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.CreateFoundationDTO;
import org.hospitalDonation.v1.dtos.UpdateFoundationDTO;
import org.hospitalDonation.v1.entity.Foundation;

import java.util.List;

public interface FoundationService {
    List<Foundation> getAllFoundations();
    Foundation getFoundationById(int id);
    List<Foundation> getFoundationsByHospitalId(Integer hospitalId);
    Foundation createFoundation(CreateFoundationDTO createFoundationDTO);
    Foundation updateFoundation(int id, UpdateFoundationDTO updateFoundationDTO);
    void deleteFoundation(int id);
}
