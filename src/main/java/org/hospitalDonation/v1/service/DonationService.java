package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.CreateDonationDTO;
import org.hospitalDonation.v1.entity.Donation;

import java.util.List;

public interface DonationService {
    Donation createDonation(CreateDonationDTO dto) throws Exception;
    List<Donation> getDonationsByHospitalId(Integer hospitalId);
    List<Donation> getDonationsByFoundationId(Integer foundationId);
    List<Donation> getDonationsByCitizenId(String citizenId);
}
