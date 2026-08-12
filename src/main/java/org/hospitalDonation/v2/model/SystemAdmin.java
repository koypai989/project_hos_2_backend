package org.hospitalDonation.v2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "system_admin")
@PrimaryKeyJoinColumn(name = "user_id")
public class SystemAdmin extends User {
    private Integer adminId;

    // Getters and Setters
}