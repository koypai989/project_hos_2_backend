package org.hospitalDonation.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SystemAdminRegistrationDTO {
    private String email;
    private String password;
    private String role;

    private String fullName;
}
