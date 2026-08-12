package org.hospitalDonation.v1.dtos;

import lombok.Getter;
import lombok.Setter;

// ข้อมูลธุรกรรมที่ parse ได้จาก slip-c
@Getter
@Setter
public class SlipData {
    private String ref;
    private String date;
    private Double amount;
    private String senderBank;
    private String senderName;
    private String receiverBank;
    private String receiverName;
}
