package org.hospitalDonation.v2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "foundation")
public class Foundation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer foundationId;
    private String foundationName;
    private String description;
    private String logo;
    private String signature;
    private Double target;
    private String qrCode;
    private String verificationDoc;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    // Getters and Setters
}