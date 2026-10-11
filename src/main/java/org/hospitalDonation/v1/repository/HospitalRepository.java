package org.hospitalDonation.v1.repository;

import org.hospitalDonation.v1.entity.Hospital;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
    List<Hospital> findByVerificationStatusIn(List<String> statuses);
    List<Hospital> findByHospitalNameContainingIgnoreCaseAndVerificationStatus(String keyword, String status);

    // โรงพยาบาลที่อนุมัติแล้ว เรียงตามจำนวนครั้งที่ได้รับบริจาคจากน้อยไปมาก
    // (ใช้แนะนำโรงพยาบาลที่ยังได้รับบริจาคน้อย)
    @Query("SELECT h FROM Hospital h WHERE h.verificationStatus = 'APPROVED' " +
           "ORDER BY (SELECT COUNT(d) FROM Donation d WHERE d.hospital = h) ASC, h.hospitalId ASC")
    List<Hospital> findApprovedOrderByDonationCountAsc(Pageable pageable);
}