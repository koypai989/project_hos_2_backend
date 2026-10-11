package org.hospitalDonation.v1.service.slip;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// อ่าน QR "ตรวจสอบสลิป" มาตรฐานธนาคารไทย (mini QR บนสลิปทุกธนาคาร)
// รูปแบบ TLV: 00{len}[00 06 "000001"][01 03 รหัสธนาคารผู้โอน][02 nn เลขอ้างอิง] 51 02 "TH" 91 04 CRC
// ตัวอย่าง: 0041000600000101030040220016284125852BPM050435102TH910481DA
public final class SlipQrReader {

    private static final String SLIP_API_ID = "000001";

    public record QrSlipInfo(String sendingBank, String transRef) {}

    private SlipQrReader() {}

    public static QrSlipInfo read(BufferedImage image) {
        String payload = decodeQr(image);
        if (payload == null) {
            throw new SlipVerifyException(422, "qr-not-found",
                    "ไม่พบ QR code ในรูปสลิป กรุณาแนบรูปสลิปที่ชัดเจนและเห็น QR ครบถ้วน");
        }
        return parse(payload);
    }

    static QrSlipInfo parse(String payload) {
        Map<String, String> top = parseTlv(payload);
        Map<String, String> slip = top.get("00") == null ? null : parseTlv(top.get("00"));

        // QR พร้อมเพย์สำหรับ "จ่ายเงิน" ก็มี tag 00 แต่ไม่ใช่ API ตรวจสลิป → ไม่นับเป็นสลิป
        if (slip == null || !SLIP_API_ID.equals(slip.get("00"))
                || slip.get("01") == null || slip.get("02") == null) {
            throw new SlipVerifyException(422, "invalid-slip-qr",
                    "QR ในรูปไม่ใช่ QR ตรวจสอบสลิปของธนาคาร กรุณาแนบสลิปการโอนเงินจริง");
        }

        // ตรวจ CRC กันการแก้ไขข้อมูลใน QR
        String crc = top.get("91");
        if (crc != null) {
            int idx = payload.lastIndexOf("91" + "04" + crc);
            String expected = crc16(payload.substring(0, idx + 4));
            if (!expected.equalsIgnoreCase(crc)) {
                throw new SlipVerifyException(422, "invalid-slip-qr",
                        "QR บนสลิปไม่ถูกต้อง (checksum ไม่ตรง) สลิปอาจถูกแก้ไข");
            }
        }
        return new QrSlipInfo(slip.get("01"), slip.get("02"));
    }

    private static String decodeQr(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = image.getRGB(0, 0, w, h, null, 0, w);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(w, h, pixels)));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        try {
            return new MultiFormatReader().decode(bitmap, hints).getText();
        } catch (NotFoundException e) {
            return null;
        }
    }

    // แยก TLV: tag 2 หลัก + ความยาว 2 หลัก + ค่า
    private static Map<String, String> parseTlv(String s) {
        Map<String, String> out = new LinkedHashMap<>();
        int i = 0;
        try {
            while (i + 4 <= s.length()) {
                String tag = s.substring(i, i + 2);
                int len = Integer.parseInt(s.substring(i + 2, i + 4));
                out.put(tag, s.substring(i + 4, i + 4 + len));
                i += 4 + len;
            }
        } catch (RuntimeException e) {
            throw new SlipVerifyException(422, "invalid-slip-qr",
                    "อ่านข้อมูล QR บนสลิปไม่ได้ กรุณาแนบสลิปการโอนเงินจริง");
        }
        return out;
    }

    // CRC-16/CCITT-FALSE (poly 0x1021, init 0xFFFF) แบบเดียวกับ QR พร้อมเพย์
    private static String crc16(String data) {
        int crc = 0xFFFF;
        for (byte b : data.getBytes(StandardCharsets.US_ASCII)) {
            crc ^= (b & 0xFF) << 8;
            for (int k = 0; k < 8; k++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
