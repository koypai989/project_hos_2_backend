package org.hospitalDonation.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HospitalProfileUpdateDTO {
    private String firstName;
    private String lastName;
    private String phone;

    private String hospitalName;
    private String address;
    private String description;
    private String logo;
    private String qrCode;
    private String signature;
    private String signatureName;
    private String verificationDoc;
}