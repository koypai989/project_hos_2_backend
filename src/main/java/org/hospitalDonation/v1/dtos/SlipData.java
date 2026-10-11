package org.hospitalDonation.v1.dtos;

import lombok.Getter;
import lombok.Setter;

// ข้อมูลธุรกรรมที่อ่านได้จากสลิป (QR + OCR) — ฟิลด์ตรงกับ data ของ API ตรวจสลิปเดิม
@Getter
@Setter
public class SlipData {
    private String ref;
    private String date;
    private Double amount;
    private String senderBank;
    private String senderName;
    private String senderId;
    private String receiverBank;
    private String receiverName;
    private String receiverId;
}
