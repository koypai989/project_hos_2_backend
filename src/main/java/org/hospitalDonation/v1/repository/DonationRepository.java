package org.hospitalDonation.v1.repository;

import org.hospitalDonation.v1.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Integer> {
    boolean existsByRefNumber(String refNumber);
    List<Donation> findByHospital_HospitalId(Integer hospitalId);
    List<Donation> findByFoundation_FoundationId(Integer foundationId);
    List<Donation> findByDonator_CitizenID(String citizenId);
}
