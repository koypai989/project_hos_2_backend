package org.hospitalDonation.v1.service;

import org.hospitalDonation.v1.dtos.SlipData;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// เรียกบริการ slip-c (https://slip-c.oiio.download) เพื่อตรวจสอบสลิปโอนเงิน
@Service
public class SlipVerifyService {

    private static final String BASE_URL = "https://slip-c.oiio.download";
    private static final int MAX_ATTEMPTS = 2; // ลองซ้ำ 1 ครั้งถ้าบริการภายนอกช้า/ไม่ตอบ

    private final ObjectMapper mapper; // ใช้ ObjectMapper ที่ Spring จัดการให้ (Jackson 3)
    // slip-c อาจใช้เวลาถึง 25 วิ จึงตั้ง timeout เผื่อไว้มากกว่านั้น
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public SlipVerifyService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // amount = null → ไม่ต้องยืนยันยอด ให้ slip-c อ่านยอดจากสลิปมาให้เลย (POST /api/slip)
    public SlipData verify(Double amount, String imgDataUri) throws Exception {
        String endpoint = "/api/slip";
        if (amount != null) {
            // amount ใน path: ถ้าเป็นจำนวนเต็มส่งเป็น integer, ไม่งั้นส่งทศนิยม
            String amountPath = (amount == Math.rint(amount))
                    ? String.valueOf(amount.longValue())
                    : amount.toString();
            endpoint = "/api/slip/" + amountPath;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("img", imgDataUri);
        payload.put("tos", true);
        payload.put("privacy", true);
        payload.put("eula", true);
        String body = mapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .timeout(Duration.ofSeconds(45))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return handleResponse(response);
            } catch (HttpTimeoutException | java.net.ConnectException e) {
                System.out.println("slip-c attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException(
                            "บริการตรวจสอบสลิปตอบสนองช้าเกินไป กรุณาลองใหม่อีกครั้งในอีกสักครู่");
                }
                // รอสั้นๆ ก่อนลองซ้ำ
                Thread.sleep(1000);
            } catch (IOException e) {
                System.out.println("slip-c attempt " + attempt + " connection error: " + e.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException(
                            "ไม่สามารถเชื่อมต่อบริการตรวจสอบสลิปได้ กรุณาตรวจสอบอินเทอร์เน็ตแล้วลองใหม่อีกครั้ง");
                }
                Thread.sleep(1000);
            }
        }
        // ไม่ควรมาถึงตรงนี้ได้ (ลูปด้านบน return หรือ throw เสมอ)
        throw new IllegalStateException("ตรวจสอบสลิปไม่สำเร็จ กรุณาลองใหม่อีกครั้ง");
    }

    @SuppressWarnings("unchecked")
    private SlipData handleResponse(HttpResponse<String> response) throws Exception {
        int status = response.statusCode();

        if (status != 200) {
            String detail = extractError(response.body());
            String slug = extractSlug(response.body());
            if ("qr-not-found".equals(slug)) {
                throw new IllegalStateException("ไม่พบ QR code ในรูปสลิป กรุณาแนบรูปสลิปที่ชัดเจนและเห็น QR ครบถ้วน");
            }
            switch (status) {
                case 422:
                    throw new IllegalStateException("อ่านข้อมูลจากสลิปไม่ได้ (" + detail + ")");
                case 404:
                    throw new IllegalStateException("ตรวจสอบสลิปไม่สำเร็จ ไม่พบข้อมูลสลิป (" + detail + ")");
                case 400:
                    throw new IllegalStateException("ข้อมูลสลิปไม่ถูกต้อง (" + detail + ")");
                default:
                    throw new IllegalStateException("ตรวจสอบสลิปไม่สำเร็จ [" + status + "] (" + detail + ")");
            }
        }

        Map<String, Object> root = mapper.readValue(response.body(), Map.class);
        Object dataObj = root.get("data");
        if (!(dataObj instanceof Map)) {
            throw new IllegalStateException("ไม่พบข้อมูลธุรกรรมในสลิป");
        }
        Map<String, Object> data = (Map<String, Object>) dataObj;

        SlipData result = new SlipData();
        result.setRef(str(data.get("ref")));
        result.setDate(str(data.get("date")));
        result.setAmount(toDouble(data.get("amount")));
        result.setSenderBank(str(data.get("sender_bank")));
        result.setSenderName(str(data.get("sender_name")));
        result.setReceiverBank(str(data.get("receiver_bank")));
        result.setReceiverName(str(data.get("receiver_name")));
        return result;
    }

    private String str(Object o) {
        return o == null ? null : o.toString();
    }

    private Double toDouble(Object o) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o == null) return null;
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractError(String body) {
        try {
            Map<String, Object> node = mapper.readValue(body, Map.class);
            if (node.get("message") != null) return node.get("message").toString();
            if (node.get("error") != null) return node.get("error").toString();
            if (node.get("slug") != null) return node.get("slug").toString();
        } catch (Exception ignored) {
        }
        return body == null ? "" : body;
    }

    @SuppressWarnings("unchecked")
    private String extractSlug(String body) {
        try {
            Map<String, Object> node = mapper.readValue(body, Map.class);
            if (node.get("slug") != null) return node.get("slug").toString();
        } catch (Exception ignored) {
        }
        return null;
    }
}
