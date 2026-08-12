package org.hospitalDonation.v1.repository;

import org.hospitalDonation.v1.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
    List<Hospital> findByVerificationStatusIn(List<String> statuses);
    List<Hospital> findByHospitalNameContainingIgnoreCaseAndVerificationStatus(String keyword, String status);
}