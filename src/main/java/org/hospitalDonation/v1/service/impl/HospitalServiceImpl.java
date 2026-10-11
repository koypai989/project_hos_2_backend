package org.hospitalDonation.v1.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hospitalDonation.v1.dtos.CreateHospitalDTO;
import org.hospitalDonation.v1.dtos.HospitalProfileUpdateDTO;
import org.hospitalDonation.v1.dtos.HospitalRequestDTO;
import org.hospitalDonation.v1.entity.Hospital;
import org.hospitalDonation.v1.entity.HospitalAdmin;
import org.hospitalDonation.v1.repository.HospitalAdminRepository;
import org.hospitalDonation.v1.repository.HospitalRepository;
import org.hospitalDonation.v1.service.HospitalService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalServiceImpl implements HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalAdminRepository hospitalAdminRepository;

    @Override
    public Hospital getHospitalById(int id) {
        return hospitalRepository.getReferenceById(id);
    }

    @Override
    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    @Override
    public void createHospital(CreateHospitalDTO dto) {
        Hospital hospital = Hospital.builder()
                .hospitalName(dto.getHospitalName())
                .address(dto.getAddress())
                .description(dto.getDescription())
                .logo(dto.getLogo())
                .qrCode(dto.getQrCode())
                .signature(dto.getSignature())
                .signatureName(dto.getSignatureName())
                .verificationStatus(dto.getVerificationStatus())
                .verificationDoc(dto.getVerificationDoc())
                .build();
        hospitalRepository.save(hospital);
    }
    @Override
    public List<Hospital> getPendingHospitals() {

        List<String> targetStatuses = Arrays.asList("PENDING");
        return hospitalRepository.findByVerificationStatusIn(targetStatuses);
    }

    // คำขอที่รออนุมัติ + ข้อมูลผู้ยื่นคำขอ เพื่อให้แอดมินเห็นข้อมูลครบก่อนตัดสินใจ
    @Override
    public List<HospitalRequestDTO> getPendingHospitalRequests() {
        return getPendingHospitals().stream()
                .map(h -> HospitalRequestDTO.from(
                        h,
                        hospitalAdminRepository.findByHospital_HospitalId(h.getHospitalId()).orElse(null)))
                .toList();
    }

    @Override
    public List<Hospital> getApproveHospitals() {
        List<String> targetStatuses = Arrays.asList("APPROVED");
        return hospitalRepository.findByVerificationStatusIn(targetStatuses);
    }

    @Override
    public void approveHospitalById(int id) {
        Hospital hospital = hospitalRepository.getReferenceById(id);
        hospital.setVerificationStatus("APPROVED");
        hospitalRepository.save(hospital);
    }

    @Override
    public void rejectHospitalById(int id) {
        Hospital hospital = hospitalRepository.getReferenceById(id);
        hospital.setVerificationStatus("REJECT");
        hospitalRepository.save(hospital);
    }

    @Override
    public List<Hospital> searchApprovedHospitals(String keyword) {
        return hospitalRepository.findByHospitalNameContainingIgnoreCaseAndVerificationStatus(keyword, "APPROVED");
    }

    // แนะนำโรงพยาบาลที่ได้รับบริจาคน้อยที่สุด
    @Override
    public List<Hospital> getSuggestedHospitals(int limit) {
        int size = limit <= 0 ? 5 : limit;
        return hospitalRepository.findApprovedOrderByDonationCountAsc(PageRequest.of(0, size));
    }

    @Override
    public HospitalAdmin getHospitalAdminByUserId(Integer userId) {
        return hospitalAdminRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Hospital Admin not found"));
    }

    @Override
    @Transactional
    public void updateHospitalProfile(Integer userId, HospitalProfileUpdateDTO dto) {
        HospitalAdmin admin = getHospitalAdminByUserId(userId);

        if (dto.getFirstName() != null) admin.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) admin.setLastName(dto.getLastName());
        if (dto.getPhone() != null) admin.setPhone(dto.getPhone());
        hospitalAdminRepository.save(admin);

        Hospital hospital = admin.getHospital();
        if (dto.getHospitalName() != null) hospital.setHospitalName(dto.getHospitalName());
        if (dto.getAddress() != null) hospital.setAddress(dto.getAddress());
        if (dto.getDescription() != null) hospital.setDescription(dto.getDescription());

        if (dto.getLogo() != null) hospital.setLogo(dto.getLogo());
        if (dto.getQrCode() != null) hospital.setQrCode(dto.getQrCode());
        if (dto.getSignature() != null) hospital.setSignature(dto.getSignature());
        if (dto.getSignatureName() != null) hospital.setSignatureName(dto.getSignatureName());
        if (dto.getVerificationDoc() != null) hospital.setVerificationDoc(dto.getVerificationDoc());

        hospitalRepository.save(hospital);
    }

}