package org.hospitalDonation.v1.dtos;

import lombok.Getter;
import lombok.Setter;
import org.hospitalDonation.v1.entity.User;

// ข้อมูลผู้ใช้แบบปลอดภัย (ไม่มี password) สำหรับหน้าจัดการผู้ใช้
@Getter
@Setter
public class UserResponseDTO {
    private Integer userId;
    private String email;
    private String role;

    public static UserResponseDTO from(User u) {
        UserResponseDTO d = new UserResponseDTO();
        d.setUserId(u.getUserId());
        d.setEmail(u.getEmail());
        d.setRole(u.getRole());
        return d;
    }
}
