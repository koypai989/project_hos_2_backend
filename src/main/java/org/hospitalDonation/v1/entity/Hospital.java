package org.hospitalDonation.v1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer hospitalId;

    private String hospitalName;
    private String address;
    private String description;
    private String logo;
    private String qrCode;
    private String signature;
    private String verificationStatus;
    private String verificationDoc;

}
