package org.hospitalDonation.v2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hospital_admin")
@PrimaryKeyJoinColumn(name = "user_id")
public class HospitalAdmin extends User {
    private Integer hospitalAdminId;
    private String phoine; // ตาม Diagram สะกด phoine

    @OneToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    // Getters and Setters
}