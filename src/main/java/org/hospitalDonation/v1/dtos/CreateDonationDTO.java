package org.hospitalDonation.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateDonationDTO {
    private Double amount;
    private String citizenId;   // ผู้บริจาค
    private Integer hospitalId;  // บริจาคให้โรงพยาบาล (อาจเป็น null)
    private Integer foundationId;// บริจาคให้มูลนิธิ (อาจเป็น null)
    private String slipBase64;   // รูปสลิปแบบ data URI (data:image/jpeg;base64,...)
}
