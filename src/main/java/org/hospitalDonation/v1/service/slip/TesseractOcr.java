package org.hospitalDonation.v1.service.slip;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// เรียก Tesseract OCR (ติดตั้งแยกบนเครื่อง) ผ่าน command line
// คืนข้อความเป็น "บรรทัด" เรียงจากบนลงล่าง โดยตัดบรรทัดเมื่อคำห่างกันมาก
// (กันข้อความของแบนเนอร์/QR ด้านขวาไปปนกับชื่อผู้โอน/ผู้รับ)
@Component
public class TesseractOcr {

    private static final long TIMEOUT_SECONDS = 30;

    private final String tesseractPath;
    private final String tessdataDir; // null = ใช้ tessdata ที่มากับตัวติดตั้ง
    private final String languages;   // เช่น "tha+eng"

    public TesseractOcr(
            @Value("${slip.ocr.tesseract-path:C:/Program Files/Tesseract-OCR/tesseract.exe}") String tesseractPath,
            @Value("${slip.ocr.tessdata-dir:tessdata}") String tessdataDir) {
        this.tesseractPath = tesseractPath;

        // ใช้โฟลเดอร์ tessdata ของโปรเจกต์ถ้ามีไฟล์ภาษา ไม่งั้นใช้ของตัวติดตั้ง
        File projectDir = new File(tessdataDir);
        File installDir = new File(new File(tesseractPath).getParentFile(), "tessdata");
        File effective = hasTrainedData(projectDir) ? projectDir : installDir;
        this.tessdataDir = hasTrainedData(projectDir) ? projectDir.getAbsolutePath() : null;

        boolean tha = new File(effective, "tha.traineddata").exists();
        boolean eng = new File(effective, "eng.traineddata").exists();
        this.languages = tha && eng ? "tha+eng" : tha ? "tha" : "eng";
        System.out.println("Slip OCR: tesseract=" + tesseractPath
                + " tessdata=" + effective.getAbsolutePath() + " lang=" + languages);
    }

    // อ่านภาษาไทยได้ไหม (ถ้าไม่ได้ ชื่อผู้โอน/ผู้รับ/เดือนภาษาไทย จะอ่านไม่ออก)
    public boolean supportsThai() {
        return languages.contains("tha");
    }

    public List<String> readLines(BufferedImage image) {
        Path input = null;
        try {
            input = Files.createTempFile("slip-ocr-", ".png");
            ImageIO.write(preprocess(image), "png", input.toFile());

            List<String> cmd = new ArrayList<>(List.of(
                    tesseractPath, input.toString(), "stdout",
                    "-l", languages, "--psm", "3", "tsv"));
            if (tessdataDir != null) {
                cmd.add(3, "--tessdata-dir");
                cmd.add(4, tessdataDir);
            }

            Process process = new ProcessBuilder(cmd).redirectErrorStream(false).start();
            byte[] out = process.getInputStream().readAllBytes();
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new SlipVerifyException(500, "ocr-timeout", "อ่านข้อความจากสลิปนานเกินไป กรุณาลองใหม่");
            }
            if (process.exitValue() != 0) {
                String err = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new SlipVerifyException(500, "ocr-failed", "อ่านข้อความจากสลิปไม่สำเร็จ (" + err.trim() + ")");
            }
            return toLines(new String(out, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SlipVerifyException(500, "ocr-unavailable",
                    "ไม่พบโปรแกรม OCR บนเซิร์ฟเวอร์ (ตั้งค่า slip.ocr.tesseract-path)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SlipVerifyException(500, "ocr-failed", "อ่านข้อความจากสลิปไม่สำเร็จ");
        } finally {
            if (input != null) {
                try { Files.deleteIfExists(input); } catch (IOException ignored) { }
            }
        }
    }

    // ขยาย 2 เท่า + แปลงเป็นขาวดำ ช่วยให้ Tesseract อ่านตัวหนังสือเล็กบนสลิปได้แม่นขึ้น
    private BufferedImage preprocess(BufferedImage src) {
        int w = src.getWidth() * 2;
        int h = src.getHeight() * 2;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private record Word(int left, int width, int height, String text) {}

    // TSV: level page block par line word left top width height conf text
    private List<String> toLines(String tsv) {
        Map<String, List<Word>> lines = new LinkedHashMap<>();
        for (String row : tsv.split("\\R")) {
            String[] c = row.split("\t", -1);
            if (c.length < 12 || !"5".equals(c[0])) continue; // level 5 = คำ
            String text = c[11].trim();
            if (text.isEmpty()) continue;
            String key = c[2] + "-" + c[3] + "-" + c[4];
            lines.computeIfAbsent(key, k -> new ArrayList<>()).add(new Word(
                    Integer.parseInt(c[6]), Integer.parseInt(c[8]), Integer.parseInt(c[9]), text));
        }

        List<String> result = new ArrayList<>();
        for (List<Word> words : lines.values()) {
            words.sort(Comparator.comparingInt(Word::left));
            int lineHeight = words.stream().mapToInt(Word::height).max().orElse(20);
            StringBuilder seg = new StringBuilder();
            int prevRight = -1;
            for (Word w : words) {
                // ช่องว่างกว้างกว่า ~3 เท่าความสูงตัวอักษร = คนละคอลัมน์ → ตัดเป็นบรรทัดใหม่
                if (prevRight >= 0 && w.left() - prevRight > lineHeight * 3) {
                    result.add(seg.toString());
                    seg.setLength(0);
                }
                // ภาษาไทยไม่เว้นวรรคระหว่างคำ แต่ Tesseract ตัดเป็นหลายกล่อง
                // → เว้นวรรคเฉพาะเมื่อมีช่องว่างจริงในภาพ
                if (!seg.isEmpty() && w.left() - prevRight > lineHeight * 0.3) seg.append(' ');
                seg.append(w.text());
                prevRight = w.left() + w.width();
            }
            if (!seg.isEmpty()) result.add(seg.toString());
        }
        return result;
    }

    private static boolean hasTrainedData(File dir) {
        File[] files = dir.isDirectory() ? dir.listFiles((d, n) -> n.endsWith(".traineddata")) : null;
        return files != null && files.length > 0;
    }
}
