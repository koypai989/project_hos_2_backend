package org.hospitalDonation.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// ใช้ให้ System Admin แก้ข้อมูลผู้ใช้ (partial update)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {
    private String email;
    private String role;
    private String password; // ถ้าไม่ null/ว่าง = ตั้งรหัสผ่านใหม่
}
