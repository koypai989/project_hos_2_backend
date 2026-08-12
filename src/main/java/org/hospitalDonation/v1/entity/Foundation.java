package org.hospitalDonation.v1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "foundation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Foundation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "foundation_id")
    private Integer foundationId;

    @Column(name = "foundation_name", nullable = false, length = 100)
    private String foundationName;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "logo", nullable = false, length = 255)
    private String logo;

    @Column(name = "signature", nullable = false, length = 255)
    private String signature;

    @Column(name = "target_amount", nullable = false)
    private Double targetAmount;

    @Column(name = "qr_code", nullable = false, length = 255)
    private String qrCode;

    @Column(name = "verification_doc", nullable = false, length = 255)
    private String verificationDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
}