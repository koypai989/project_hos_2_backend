package org.hospitalDonation.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateFoundationDTO {
    private String foundationName;
    private String description;
    private String logo;
    private String signature;
    private String signatureName;
    private Double targetAmount;
    private String qrCode;
    private String verificationDoc;
    private Integer hospitalId;
}
