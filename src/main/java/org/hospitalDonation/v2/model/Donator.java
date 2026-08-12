package org.hospitalDonation.v2.model;
import jakarta.persistence.*;

@Entity
@Table(name = "donator")
@PrimaryKeyJoinColumn(name = "user_id")
public class Donator extends User {
    private String citizenID; // ตาม Diagram สะกด ID ตัวใหญ่
    private java.util.Date dateOfBirth;
    private String adress; // ตาม Diagram สะกด adress (s ตัวเดียว)

    // Getters and Setters
}