package org.hospitalDonation.v1.repository;

import org.hospitalDonation.v1.entity.Foundation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoundationRepository extends JpaRepository<Foundation, Integer> {
    List<Foundation> findByHospital_HospitalId(Integer hospitalId);
}
