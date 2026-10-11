package org.hospitalDonation.v1.service.slip;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlipParsingTest {

    // payload จริงจาก QR บนสลิปกสิกรไทย (bath1.jpg)
    private static final String KBANK_QR = "0041000600000101030040220016284125852BPM050435102TH910481DA";

    @Test
    void readsRefAndBankFromSlipQr() {
        SlipQrReader.QrSlipInfo info = SlipQrReader.parse(KBANK_QR);
        assertEquals("004", info.sendingBank());
        assertEquals("016284125852BPM05043", info.transRef());
    }

    @Test
    void rejectsTamperedQr() {
        // แก้เลขอ้างอิง 1 หลัก แต่ CRC เดิม
        String tampered = KBANK_QR.replace("BPM05043", "BPM05044");
        SlipVerifyException e = assertThrows(SlipVerifyException.class, () -> SlipQrReader.parse(tampered));
        assertEquals("invalid-slip-qr", e.getSlug());
    }

    @Test
    void rejectsPromptPayPaymentQr() {
        // QR พร้อมเพย์สำหรับจ่ายเงิน (ไม่ใช่ QR ตรวจสลิป)
        String promptPay = "00020101021129370016A000000677010111011300668123456785802TH530376463048956";
        assertThrows(SlipVerifyException.class, () -> SlipQrReader.parse(promptPay));
    }

    @Test
    void parsesThaiKbankSlipText() {
        // ข้อความตามเลย์เอาต์สลิป K+ (bath1.jpg) เมื่อ OCR ภาษาไทยได้
        List<String> lines = List.of(
                "จ่ายบิลสำเร็จ",
                "11 ต.ค. 69 12:58 น.",
                "ด.ช. ชินกฤต พ",
                "ธ.กสิกรไทย",
                "THAILAND 2026",
                "xxx-x-x9422-x",
                "โรงพยาบาลนครพิงค์",
                "0994000423012003",
                "xxxxxxxxx6144",
                "จำนวน:",
                "1.00 บาท",
                "ค่าธรรมเนียม:",
                "0.00 บาท",
                "เลขที่รายการ:",
                "016284125852BPM05043");
        SlipTextParser.Result r = SlipTextParser.parse(lines, true);
        assertEquals(1.00, r.amount());
        assertEquals("2026-10-11T05:58:00.000Z", r.dateIso()); // 12:58 เวลาไทย = 05:58 UTC
        assertEquals("ด.ช. ชินกฤต พ", r.senderName());
        assertEquals("xxx-x-x9422-x", r.senderId());
        assertEquals("โรงพยาบาลนครพิงค์", r.receiverName());
        assertEquals("xxxxxxxxx6144", r.receiverId());
    }

    @Test
    void parsesThaiDigitsAndFullYearAndThousands() {
        List<String> lines = List.of(
                "โอนเงินสำเร็จ",
                "๓ มี.ค. 2569 - 09:05",
                "นาย สมชาย ใจดี",
                "ธนาคารไทยพาณิชย์",
                "xxx-xxx123-4",
                "นางสาว สมหญิง รักดี",
                "ธ.กรุงไทย",
                "xxx-x-x5678-x",
                "จำนวนเงิน 12,500.50 บาท",
                "ค่าธรรมเนียม 0.00 บาท");
        SlipTextParser.Result r = SlipTextParser.parse(lines, true);
        assertEquals(12500.50, r.amount());
        assertEquals("2026-03-03T02:05:00.000Z", r.dateIso());
        assertEquals("นาย สมชาย ใจดี", r.senderName());
        assertEquals("นางสาว สมหญิง รักดี", r.receiverName());
        assertEquals("006", r.receiverBank());
    }

    @Test
    void amountFallsBackToFirstMoneyWhenLabelsUnreadable() {
        // ผลจริงจาก OCR ภาษาอังกฤษอย่างเดียว (ป้ายภาษาไทยอ่านไม่ออก)
        List<String> lines = List.of("Nataaduse IX", "awa:", "1.00 um", "mnassuuiien:", "0.00 win");
        SlipTextParser.Result r = SlipTextParser.parse(lines, false);
        assertEquals(1.00, r.amount());
        assertNull(r.senderName()); // ไม่มีภาษาไทย → ไม่เดาชื่อ
    }
}
