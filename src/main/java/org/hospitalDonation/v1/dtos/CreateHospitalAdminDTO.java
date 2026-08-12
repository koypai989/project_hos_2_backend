package org.hospitalDonation.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateHospitalAdminDTO {
    private String firstName;
    private String lastName;
    private String phone;
    private Integer userId;       // รับเป็น ID เพื่อไปผูกกับตาราง User
    private Integer hospitalId;   // รับเป็น ID เพื่อไปผูกกับตาราง Hospital
}