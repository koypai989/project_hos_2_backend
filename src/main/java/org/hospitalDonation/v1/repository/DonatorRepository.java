package org.hospitalDonation.v1.repository;

import org.hospitalDonation.v1.entity.Donator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonatorRepository extends JpaRepository<Donator,String> {
    Optional<Donator> findByUser_UserId(Integer userId);
}
