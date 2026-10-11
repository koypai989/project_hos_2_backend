package org.hospitalDonation.v1.service.slip;

import lombok.Getter;

// ตรวจสลิปไม่ผ่าน — เก็บ HTTP status + slug แบบเดียวกับ API ตรวจสลิปเดิม (slip-c)
// extends IllegalStateException เพื่อให้ DonationController ตอบ 400 พร้อมข้อความไทยได้เหมือนเดิม
@Getter
public class SlipVerifyException extends IllegalStateException {
    private final int status;
    private final String slug;

    public SlipVerifyException(int status, String slug, String message) {
        super(message);
        this.status = status;
        this.slug = slug;
    }
}
