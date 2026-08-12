package org.hospitalDonation.v1.service;

public interface PasswordService {
    /**
     * สำหรับทำการ Hash รหัสผ่านก่อนบันทึกลงฐานข้อมูล
     * @param rawPassword รหัสผ่านแบบ Plaintext ที่รับมาจากหน้า Register
     * @return รหัสผ่านที่ผ่านการ Hash แล้ว
     */
    String hashPassword(String rawPassword);

    /**
     * สำหรับตรวจสอบรหัสผ่านตอน Login
     * @param rawPassword รหัสผ่านแบบ Plaintext ที่กรอกเข้ามาจากหน้า Login
     * @param hashedPassword รหัสผ่านที่เข้ารหัสแล้วในฐานข้อมูล
     * @return true หากรหัสผ่านถูกต้อง, false หากไม่ถูกต้อง
     */
    boolean verifyPassword(String rawPassword, String hashedPassword);
}
