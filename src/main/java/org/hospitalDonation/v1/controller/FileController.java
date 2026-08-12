package org.hospitalDonation.v1.controller;

import org.hospitalDonation.dtos.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("v1/files")
public class FileController {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<ResponseObject> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ทำความสะอาดชื่อไฟล์: อนุญาตเฉพาะ ASCII ที่ปลอดภัย เพื่อกันปัญหา encoding
            // ของชื่อไฟล์ที่มีตัวอักษรไทย/Unicode (ซึ่งจะเพี้ยนตอนเซฟลงดิสก์ทำให้เสิร์ฟรูปไม่ได้)
            String original = file.getOriginalFilename();
            if (original == null) original = "file";

            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot).replaceAll("[^a-zA-Z0-9.]", "");
                original = original.substring(0, dot);
            }

            String safeBase = original
                    .replaceAll("[^a-zA-Z0-9._-]", "_") // แทนอักขระที่ไม่ปลอดภัยด้วย _
                    .replaceAll("_+", "_")               // ยุบ _ ที่ติดกัน
                    .replaceAll("^_|_$", "");            // ตัด _ หัว/ท้าย
            if (safeBase.isEmpty()) safeBase = "file";

            String fileName = System.currentTimeMillis() + "_" + safeBase + ext;
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "/images/" + fileName;

            return new ResponseEntity<>(new ResponseObject(true, "Upload success", fileUrl), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseObject(false, "Upload failed: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}