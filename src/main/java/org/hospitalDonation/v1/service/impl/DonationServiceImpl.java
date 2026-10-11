package org.hospitalDonation.v1.service.impl;

import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.CreateDonationDTO;
import org.hospitalDonation.v1.dtos.SlipData;
import org.hospitalDonation.v1.entity.Donation;
import org.hospitalDonation.v1.entity.Donator;
import org.hospitalDonation.v1.entity.Foundation;
import org.hospitalDonation.v1.entity.Hospital;
import org.hospitalDonation.v1.repository.DonationRepository;
import org.hospitalDonation.v1.repository.DonatorRepository;
import org.hospitalDonation.v1.repository.FoundationRepository;
import org.hospitalDonation.v1.repository.HospitalRepository;
import org.hospitalDonation.v1.service.CertificateService;
import org.hospitalDonation.v1.service.DonationService;
import org.hospitalDonation.v1.service.SlipVerifyService;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;
    private final DonatorRepository donatorRepository;
    private final HospitalRepository hospitalRepository;
    private final FoundationRepository foundationRepository;
    private final SlipVerifyService slipVerifyService;
    private final CertificateService certificateService;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public Donation createDonation(CreateDonationDTO dto) throws Exception {
        // ตรวจ input เบื้องต้น (amount ไม่บังคับ — อ่านจากสลิปให้เลย)
        if (dto.getAmount() != null && dto.getAmount() <= 0) {
            throw new IllegalStateException("จำนวนเงินไม่ถูกต้อง");
        }
        if (dto.getSlipBase64() == null || dto.getSlipBase64().isEmpty()) {
            throw new IllegalStateException("กรุณาแนบสลิป");
        }
        if (dto.getHospitalId() == null && dto.getFoundationId() == null) {
            throw new IllegalStateException("ต้องระบุปลายทางการบริจาค (โรงพยาบาลหรือมูลนิธิ)");
        }

        Donator donator = donatorRepository.findById(dto.getCitizenId())
                .orElseThrow(() -> new NoSuchElementException("ไม่พบข้อมูลผู้บริจาค"));

        // 1. ตรวจสอบสลิปภายในระบบ (QR + OCR) — dto.amount เป็น null ได้ ระบบอ่านยอดจากสลิปเอง
        SlipData slip = slipVerifyService.verify(dto.getAmount(), dto.getSlipBase64());

        // 2. ยอดเงินที่ใช้จริง = ยอดในสลิป (ถ้าผู้ใช้ระบุมาด้วยต้องตรงกัน)
        if (slip.getAmount() == null || slip.getAmount() <= 0) {
            throw new IllegalStateException("อ่านยอดเงินจากสลิปไม่ได้ กรุณาแนบสลิปที่ชัดเจนอีกครั้ง");
        }
        if (dto.getAmount() != null && Math.abs(slip.getAmount() - dto.getAmount()) > 0.001) {
            throw new IllegalStateException("ยอดเงินในสลิปไม่ตรงกับที่ระบุ");
        }
        final double amount = slip.getAmount();

        // 3. กันใช้สลิปซ้ำด้วยเลขอ้างอิง
        if (slip.getRef() != null && donationRepository.existsByRefNumber(slip.getRef())) {
            throw new IllegalStateException("สลิปนี้ถูกใช้บริจาคไปแล้ว");
        }

        // 4. บันทึกไฟล์สลิป
        String slipPath = saveBase64Image(dto.getSlipBase64());

        // 5. ปลายทาง (ดึงข้อมูลเต็มเพื่อใช้สร้างใบเซอร์)
        Hospital hospital = null;
        Foundation foundation = null;
        String orgName;
        String signatureName;
        String signaturePath;
        boolean isFoundation;
        if (dto.getHospitalId() != null) {
            hospital = hospitalRepository.findById(dto.getHospitalId())
                    .orElseThrow(() -> new NoSuchElementException("ไม่พบโรงพยาบาล"));
            orgName = hospital.getHospitalName();
            signatureName = hospital.getSignatureName();
            signaturePath = hospital.getSignature();
            isFoundation = false;
        } else {
            foundation = foundationRepository.findById(dto.getFoundationId())
                    .orElseThrow(() -> new NoSuchElementException("ไม่พบมูลนิธิ"));
            orgName = foundation.getFoundationName();
            signatureName = foundation.getSignatureName();
            signaturePath = foundation.getSignature();
            isFoundation = true;
        }

        // 6. วันเวลาจากสลิป (ถ้า parse ไม่ได้ ใช้เวลาปัจจุบัน)
        LocalDateTime donationDate = parseDate(slip.getDate());

        // 7. สร้างใบอนุโมทนาบัตร (ถ้าสร้างไม่ได้ ไม่ให้ล้มการบริจาค certPath = null)
        String donorName = ((donator.getFirstName() == null ? "" : donator.getFirstName())
                + " " + (donator.getLastName() == null ? "" : donator.getLastName())).trim();
        String certPath = certificateService.generate(
                donorName, amount, orgName,
                signatureName, signaturePath, isFoundation, donationDate);

        Donation donation = Donation.builder()
                .amount(amount)
                .donationDate(donationDate)
                .slipImagePath(slipPath)
                .certificatePath(certPath)
                .refNumber(slip.getRef())
                .donator(donator)
                .hospital(hospital)
                .foundation(foundation)
                .build();

        return donationRepository.save(donation);
    }

    @Override
    public List<Donation> getDonationsByHospitalId(Integer hospitalId) {
        return donationRepository.findByHospital_HospitalId(hospitalId);
    }

    @Override
    public List<Donation> getDonationsByFoundationId(Integer foundationId) {
        return donationRepository.findByFoundation_FoundationId(foundationId);
    }

    @Override
    public List<Donation> getDonationsByCitizenId(String citizenId) {
        return donationRepository.findByDonator_CitizenID(citizenId);
    }

    private LocalDateTime parseDate(String iso) {
        if (iso == null || iso.isEmpty()) return LocalDateTime.now();
        try {
            return LocalDateTime.ofInstant(Instant.parse(iso), ZoneId.of("Asia/Bangkok"));
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String saveBase64Image(String dataUri) throws Exception {
        String base64 = dataUri;
        String ext = "png";
        if (dataUri.startsWith("data:")) {
            int comma = dataUri.indexOf(',');
            String meta = dataUri.substring(5, comma); // เช่น image/jpeg;base64
            base64 = dataUri.substring(comma + 1);
            if (meta.contains("jpeg") || meta.contains("jpg")) ext = "jpg";
        }
        byte[] bytes = Base64.getDecoder().decode(base64);

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String fileName = System.currentTimeMillis() + "_slip." + ext;
        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, bytes);
        return "/images/" + fileName;
    }
}
