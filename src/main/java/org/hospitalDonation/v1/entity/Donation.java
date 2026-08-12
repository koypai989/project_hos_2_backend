package org.hospitalDonation.v1.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@Entity
@Table(name = "donation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_id")
    private Integer donationId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "donation_date", nullable = false)
    private LocalDateTime donationDate;

    @Column(name = "slip_image_path", nullable = false, length = 255)
    private String slipImagePath;

    @Column(name = "certificate_path", length = 255)
    private String certificatePath;

    // เลขอ้างอิงธุรกรรมจากสลิป (จาก slip-c) ใช้กันการใช้สลิปซ้ำ
    @Column(name = "ref_number", unique = true, length = 50)
    private String refNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Donator donator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foundation_id")
    private Foundation foundation;
}