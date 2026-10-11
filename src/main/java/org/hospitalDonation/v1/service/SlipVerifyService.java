package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.SlipData;
import org.hospitalDonation.v1.service.slip.SlipQrReader;
import org.hospitalDonation.v1.service.slip.SlipTextParser;
import org.hospitalDonation.v1.service.slip.SlipVerifyException;
import org.hospitalDonation.v1.service.slip.TesseractOcr;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// ตรวจสอบสลิปเองภายในระบบ (แทน slip-c ที่ปิดให้บริการแล้ว)
//   1. อ่าน QR ตรวจสอบสลิปของธนาคาร → เลขอ้างอิง (ref) + รหัสธนาคารผู้โอน (ตรวจ CRC กันแก้ไข)
//   2. OCR ด้วย Tesseract → ยอดเงิน วันเวลา ชื่อ/เลขบัญชีผู้โอนและผู้รับ
// ไม่ต้องระบุยอดเงิน — อ่านจากสลิปให้เอง
// หมายเหตุ: ตรวจรูปแบบสลิปและข้อมูลบนสลิปเท่านั้น ไม่ได้ยืนยันกับระบบธนาคารโดยตรง
@Service
public class SlipVerifyService {

    private static final int MAX_CACHE = 500;

    private final TesseractOcr ocr;
    // แคชผลตามลายนิ้วมือรูป (SHA-256) — ส่งรูปเดิมซ้ำไม่ต้อง OCR ใหม่ (fromCache = true)
    private final Map<String, SlipData> cache = new ConcurrentHashMap<>();

    public record VerifyResult(SlipData data, boolean fromCache) {}

    public SlipVerifyService(TesseractOcr ocr) {
        this.ocr = ocr;
    }

    // amount = null → ไม่ต้องยืนยันยอด ใช้ยอดที่อ่านได้จากสลิป
    public SlipData verify(Double amount, String imgDataUri) {
        SlipData data = verifyImage(imgDataUri).data();
        if (amount != null && Math.abs(data.getAmount() - amount) > 0.001) {
            throw new SlipVerifyException(422, "amount-mismatch", "ยอดเงินในสลิปไม่ตรงกับที่ระบุ");
        }
        return data;
    }

    public VerifyResult verifyImage(String imgDataUri) {
        byte[] bytes = decodeDataUri(imgDataUri);
        String key = sha256(bytes);
        SlipData cached = cache.get(key);
        if (cached != null) {
            return new VerifyResult(cached, true);
        }

        BufferedImage image = readImage(bytes);
        SlipQrReader.QrSlipInfo qr = SlipQrReader.read(image);

        List<String> lines = ocr.readLines(image);
        SlipTextParser.Result text = SlipTextParser.parse(lines, ocr.supportsThai());
        if (text.amount() == null || text.amount() <= 0) {
            throw new SlipVerifyException(422, "amount-not-found",
                    "อ่านยอดเงินจากสลิปไม่ได้ กรุณาแนบสลิปที่ชัดเจนอีกครั้ง");
        }

        SlipData data = new SlipData();
        data.setRef(qr.transRef());
        data.setDate(text.dateIso());
        data.setAmount(text.amount());
        data.setSenderBank(qr.sendingBank());
        data.setSenderName(text.senderName());
        data.setSenderId(text.senderId());
        data.setReceiverBank(text.receiverBank());
        data.setReceiverName(text.receiverName());
        data.setReceiverId(text.receiverId());

        if (cache.size() >= MAX_CACHE) cache.clear();
        cache.put(key, data);
        return new VerifyResult(data, false);
    }

    // รูปแบบ response เดียวกับ API ตรวจสลิปเดิม: { message, fromCache, data: { ref, date, amount, ... } }
    public static Map<String, Object> toApiResponse(VerifyResult result) {
        SlipData d = result.data();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ref", d.getRef());
        data.put("date", d.getDate());
        data.put("amount", d.getAmount());
        data.put("sender_bank", d.getSenderBank());
        data.put("sender_name", d.getSenderName());
        data.put("sender_id", d.getSenderId());
        data.put("receiver_bank", d.getReceiverBank());
        data.put("receiver_name", d.getReceiverName());
        data.put("receiver_id", d.getReceiverId());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Slip processed successfully.");
        body.put("fromCache", result.fromCache());
        body.put("data", data);
        return body;
    }

    private static byte[] decodeDataUri(String dataUri) {
        if (dataUri == null || dataUri.isBlank()) {
            throw new SlipVerifyException(400, "bad-request", "กรุณาแนบรูปสลิป");
        }
        String base64 = dataUri.startsWith("data:") ? dataUri.substring(dataUri.indexOf(',') + 1) : dataUri;
        try {
            return Base64.getDecoder().decode(base64.trim());
        } catch (IllegalArgumentException e) {
            throw new SlipVerifyException(400, "bad-request", "ข้อมูลรูปสลิปไม่ถูกต้อง");
        }
    }

    private static BufferedImage readImage(byte[] bytes) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img != null) return img;
        } catch (Exception ignored) {
        }
        throw new SlipVerifyException(400, "bad-request", "ไฟล์ที่แนบไม่ใช่รูปภาพ กรุณาแนบรูปสลิป (PNG/JPG)");
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
