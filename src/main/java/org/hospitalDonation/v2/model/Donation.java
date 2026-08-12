package org.hospitalDonation.v2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "donation")
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer donationID;
    private Double amount;
    private java.util.Date donationDate;
    private String slipImagePath;
    private String certificatePath;

    @ManyToOne
    @JoinColumn(name = "donator_id") // เชื่อมโยงไปที่ Donator
    private Donator donator;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "foundation_id")
    private Foundation foundation;

    // Getters and Setters
}
