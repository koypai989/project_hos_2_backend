package org.hospitalDonation.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ใช้สำหรับแก้ไขมูลนิธิ (partial update — อัปเดตเฉพาะฟิลด์ที่ส่งมาไม่ null)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFoundationDTO {
    private String foundationName;
    private String description;
    private String logo;
    private String signature;
    private String signatureName;
    private Double targetAmount;
    private String qrCode;
    private String verificationDoc;
}
