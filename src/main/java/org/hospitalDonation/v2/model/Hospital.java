package org.hospitalDonation.v2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hospital")
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

    // Getters and Setters
}