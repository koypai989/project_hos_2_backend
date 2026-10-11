package org.hospitalDonation.v1.controller;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.service.SlipVerifyService;
import org.hospitalDonation.v1.service.slip.SlipVerifyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

// ตรวจสลิปแบบเดียวกับ API เดิม (slip-c) แต่ไม่ต้องใส่ยอดเงิน
// POST /api/v1/slips/verify  body: { "img": "data:image/jpeg;base64,..." }
@RestController
@RequestMapping("/v1/slips")
@RequiredArgsConstructor
public class SlipController {

    private final SlipVerifyService slipVerifyService;

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestBody Map<String, Object> body) {
        try {
            Object img = body == null ? null : body.get("img");
            SlipVerifyService.VerifyResult result = slipVerifyService.verifyImage(img == null ? null : img.toString());
            return ResponseEntity.ok(SlipVerifyService.toApiResponse(result));
        } catch (SlipVerifyException e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", e.getMessage());
            error.put("slug", e.getSlug());
            return ResponseEntity.status(e.getStatus()).body(error);
        }
    }
}
