package org.hospitalDonation.v1.dtos;

import lombok.Getter;
import lombok.Setter;
import org.hospitalDonation.v1.entity.Donation;

import java.time.LocalDateTime;

// ข้อมูลบริจาคแบบสรุปสำหรับส่งกลับ (ไม่ผูก entity relation ตรงๆ กันข้อมูล user รั่ว)
@Getter
@Setter
public class DonationResponseDTO {
    private Integer donationId;
    private Double amount;
    private LocalDateTime donationDate;
    private String slipImagePath;
    private String certificatePath;
    private String refNumber;
    private String targetType; // "HOSPITAL" หรือ "FOUNDATION"
    private Integer targetId;
    private String targetName;

    public static DonationResponseDTO from(Donation d) {
        DonationResponseDTO dto = new DonationResponseDTO();
        dto.setDonationId(d.getDonationId());
        dto.setAmount(d.getAmount());
        dto.setDonationDate(d.getDonationDate());
        dto.setSlipImagePath(d.getSlipImagePath());
        dto.setCertificatePath(d.getCertificatePath());
        dto.setRefNumber(d.getRefNumber());
        if (d.getHospital() != null) {
            dto.setTargetType("HOSPITAL");
            dto.setTargetId(d.getHospital().getHospitalId());
            dto.setTargetName(d.getHospital().getHospitalName());
        } else if (d.getFoundation() != null) {
            dto.setTargetType("FOUNDATION");
            dto.setTargetId(d.getFoundation().getFoundationId());
            dto.setTargetName(d.getFoundation().getFoundationName());
        }
        return dto;
    }
}
