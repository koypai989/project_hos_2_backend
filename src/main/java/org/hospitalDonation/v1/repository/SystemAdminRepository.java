package org.hospitalDonation.v1.repository;
import org.hospitalDonation.v1.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemAdminRepository extends JpaRepository<SystemAdmin, Integer> {
    Optional<SystemAdmin> findByUser_UserId(Integer userId);
}
