package org.hospitalDonation.v1.repository;

import org.hospitalDonation.v1.entity.HospitalAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HospitalAdminRepository extends JpaRepository<HospitalAdmin, Integer> {
    Optional<HospitalAdmin> findByUser_UserId(Integer userId);
}