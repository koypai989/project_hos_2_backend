package org.hospitalDonation.v1.dtos;

import lombok.Getter;
import lombok.Setter;
import org.hospitalDonation.v1.entity.Hospital;
import org.hospitalDonation.v1.entity.HospitalAdmin;

// ข้อมูลคำขอโรงพยาบาลแบบเต็มสำหรับหน้าอนุมัติของแอดมิน
// = ข้อมูลโรงพยาบาลทั้งหมด + ข้อมูลผู้ยื่นคำขอ (เจ้าหน้าที่โรงพยาบาล)
// ชื่อฟิลด์ส่วนของโรงพยาบาลตรงกับ entity Hospital เพื่อให้ฝั่งแอปใช้ model เดิมได้
@Getter
@Setter
public class HospitalRequestDTO {
    private Integer hospitalId;
    private String hospitalName;
    private String address;
    private String description;
    private String logo;
    private String qrCode;
    private String signature;
    private String signatureName;
    private String verificationStatus;
    private String verificationDoc;

    // ผู้ยื่นคำขอ
    private String adminFirstName;
    private String adminLastName;
    private String adminPhone;
    private String adminEmail;

    public static HospitalRequestDTO from(Hospital h, HospitalAdmin admin) {
        HospitalRequestDTO dto = new HospitalRequestDTO();
        dto.setHospitalId(h.getHospitalId());
        dto.setHospitalName(h.getHospitalName());
        dto.setAddress(h.getAddress());
        dto.setDescription(h.getDescription());
        dto.setLogo(h.getLogo());
        dto.setQrCode(h.getQrCode());
        dto.setSignature(h.getSignature());
        dto.setSignatureName(h.getSignatureName());
        dto.setVerificationStatus(h.getVerificationStatus());
        dto.setVerificationDoc(h.getVerificationDoc());
        if (admin != null) {
            dto.setAdminFirstName(admin.getFirstName());
            dto.setAdminLastName(admin.getLastName());
            dto.setAdminPhone(admin.getPhone());
            if (admin.getUser() != null) {
                dto.setAdminEmail(admin.getUser().getEmail());
            }
        }
        return dto;
    }
}
