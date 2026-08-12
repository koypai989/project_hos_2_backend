package org.hospitalDonation.v1.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.CreateFoundationDTO;
import org.hospitalDonation.v1.dtos.UpdateFoundationDTO;
import org.hospitalDonation.v1.entity.Foundation;
import org.hospitalDonation.v1.entity.Hospital;
import org.hospitalDonation.v1.repository.FoundationRepository;
import org.hospitalDonation.v1.repository.HospitalRepository;
import org.hospitalDonation.v1.service.FoundationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class FoundationServiceImpl implements FoundationService {

    private final FoundationRepository foundationRepository;
    private final HospitalRepository hospitalRepository;

    @Override
    public List<Foundation> getAllFoundations() {
        return foundationRepository.findAll();
    }

    @Override
    public Foundation getFoundationById(int id) {
        return foundationRepository.getReferenceById(id);
    }

    @Override
    public List<Foundation> getFoundationsByHospitalId(Integer hospitalId) {
        return foundationRepository.findByHospital_HospitalId(hospitalId);
    }

    @Override
    public Foundation createFoundation(CreateFoundationDTO dto) {
        Hospital hospital = hospitalRepository.getReferenceById(dto.getHospitalId());

        Foundation foundation = Foundation.builder()
                .foundationName(dto.getFoundationName())
                .description(dto.getDescription())
                .logo(dto.getLogo())
                .signature(dto.getSignature())
                .signatureName(dto.getSignatureName())
                .targetAmount(dto.getTargetAmount())
                .qrCode(dto.getQrCode())
                .verificationDoc(dto.getVerificationDoc())
                .hospital(hospital)
                .build();

        return foundationRepository.save(foundation);
    }

    @Override
    @Transactional
    public Foundation updateFoundation(int id, UpdateFoundationDTO dto) {
        Foundation foundation = foundationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Foundation not found"));

        // อัปเดตเฉพาะฟิลด์ที่ส่งมา (ไม่ null)
        if (dto.getFoundationName() != null) foundation.setFoundationName(dto.getFoundationName());
        if (dto.getDescription() != null) foundation.setDescription(dto.getDescription());
        if (dto.getLogo() != null) foundation.setLogo(dto.getLogo());
        if (dto.getSignature() != null) foundation.setSignature(dto.getSignature());
        if (dto.getSignatureName() != null) foundation.setSignatureName(dto.getSignatureName());
        if (dto.getTargetAmount() != null) foundation.setTargetAmount(dto.getTargetAmount());
        if (dto.getQrCode() != null) foundation.setQrCode(dto.getQrCode());
        if (dto.getVerificationDoc() != null) foundation.setVerificationDoc(dto.getVerificationDoc());

        return foundationRepository.save(foundation);
    }

    @Override
    @Transactional
    public void deleteFoundation(int id) {
        Foundation foundation = foundationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Foundation not found"));
        // soft delete: ตัดความสัมพันธ์กับโรงพยาบาล (hospital_id = null)
        // เก็บ record ไว้ในระบบ (donation ที่อ้างอิงยังอยู่) แต่ถือว่าถูกลบแล้ว
        // เพราะทุก query อิง hospital_id จึงไม่แสดงมูลนิธินี้อีก
        foundation.setHospital(null);
        foundationRepository.save(foundation);
    }
}
