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
    private static final int SCALE = 2; // ขยายรูปก่อน OCR

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

    // คืนบรรทัดเรียงจากบนลงล่าง พร้อมตำแหน่งซ้ายสุด (สัดส่วนของความกว้างรูป)
    public List<SlipTextParser.Line> readLines(BufferedImage image) {
        Path input = null;
        try {
            input = Files.createTempFile("slip-ocr-", ".png");
            BufferedImage prepared = preprocess(image);
            ImageIO.write(prepared, "png", input.toFile());

            List<String> cmd = new ArrayList<>(List.of(
                    tesseractPath, input.toString(), "stdout",
                    "-l", languages, "--psm", "3",
                    // ขอผลแบบ TSV ผ่าน -c (ไม่พึ่งไฟล์ configs/tsv ที่อาจไม่มีในโฟลเดอร์ tessdata ของโปรเจกต์)
                    "-c", "tessedit_create_tsv=1", "-c", "tessedit_create_txt=0"));
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
            return toLines(new String(out, StandardCharsets.UTF_8), image);
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
        int w = src.getWidth() * SCALE;
        int h = src.getHeight() * SCALE;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private record Word(int left, int top, int width, int height, String text) {}

    // ตำแหน่งเป็นพิกัดของรูปที่ขยายแล้ว (x SCALE)
    private record Segment(String text, int left, int top, int right, int bottom, int baseline) {}

    // TSV: level page block par line word left top width height conf text
    private List<SlipTextParser.Line> toLines(String tsv, BufferedImage original) {
        Map<String, List<Word>> lines = new LinkedHashMap<>();
        for (String row : tsv.split("\\R")) {
            String[] c = row.split("\t", -1);
            if (c.length < 12 || !"5".equals(c[0])) continue; // level 5 = คำ
            String text = c[11].trim();
            if (text.isEmpty()) continue;
            String key = c[2] + "-" + c[3] + "-" + c[4];
            lines.computeIfAbsent(key, k -> new ArrayList<>()).add(new Word(
                    Integer.parseInt(c[6]), Integer.parseInt(c[7]),
                    Integer.parseInt(c[8]), Integer.parseInt(c[9]), text));
        }

        List<Segment> segments = new ArrayList<>();
        for (List<Word> words : lines.values()) {
            words.sort(Comparator.comparingInt(Word::left));
            // ใช้ความสูง "กลางๆ" ของตัวอักษร (ไม่ใช่ค่าสูงสุด เพราะโลโก้/สระบนล่างสูงผิดปกติ)
            int charHeight = median(words.stream().mapToInt(Word::height).toArray());
            List<Word> seg = new ArrayList<>();
            int prevRight = -1;
            for (Word w : words) {
                // ช่องว่างกว้างกว่า ~3 เท่าความสูงตัวอักษร = คนละคอลัมน์ → ตัดเป็นบรรทัดใหม่
                if (prevRight >= 0 && w.left() - prevRight > charHeight * 3) {
                    segments.add(toSegment(seg, charHeight));
                    seg = new ArrayList<>();
                }
                seg.add(w);
                prevRight = w.left() + w.width();
            }
            if (!seg.isEmpty()) segments.add(toSegment(seg, charHeight));
        }

        // Tesseract เรียงตาม "บล็อก" ไม่ใช่บนลงล่าง → เรียงใหม่ตามตำแหน่งแนวตั้ง
        segments.sort(Comparator.comparingInt(Segment::baseline).thenComparingInt(Segment::left));
        List<SlipTextParser.Line> result = new ArrayList<>();
        for (Segment s : segments) {
            String text = s.text();
            // เลขบัญชีปิดบางส่วน: เทียบจำนวนตัวอักษรกับที่นับได้จริงจากพิกเซลของรูปต้นฉบับ
            if (text.toLowerCase().contains("xx")) {
                int glyphs = countGlyphs(original, s.left() / SCALE, s.top() / SCALE,
                        (s.right() + SCALE - 1) / SCALE, (s.bottom() + SCALE - 1) / SCALE);
                text = SlipTextParser.reconcileMaskedAccount(text, glyphs);
            }
            result.add(new SlipTextParser.Line(text, (double) s.left() / (original.getWidth() * SCALE)));
        }
        return result;
    }

    private Segment toSegment(List<Word> words, int charHeight) {
        StringBuilder text = new StringBuilder();
        int prevRight = -1;
        for (Word w : words) {
            // ภาษาไทยไม่เว้นวรรคระหว่างคำ แต่ Tesseract ตัดเป็นหลายกล่อง
            // → เว้นวรรคเฉพาะเมื่อมีช่องว่างจริงในภาพ
            if (prevRight >= 0 && w.left() - prevRight > charHeight * 0.5) text.append(' ');
            text.append(w.text());
            prevRight = w.left() + w.width();
        }
        int baseline = median(words.stream().mapToInt(w -> w.top() + w.height()).toArray());
        int top = words.stream().mapToInt(Word::top).min().orElse(0);
        int bottom = words.stream().mapToInt(w -> w.top() + w.height()).max().orElse(0);
        return new Segment(text.toString(), words.get(0).left(), top, prevRight, bottom, baseline);
    }

    // นับตัวอักษรในกรอบจากพิกเซลจริง: ไล่คอลัมน์ซ้ายไปขวา ช่วงคอลัมน์ที่มีจุดเข้มติดกัน = 1 ตัว
    static int countGlyphs(BufferedImage img, int x0, int y0, int x1, int y1) {
        x0 = Math.max(0, x0);
        y0 = Math.max(0, y0);
        x1 = Math.min(img.getWidth(), x1);
        y1 = Math.min(img.getHeight(), y1);
        int count = 0;
        boolean inGlyph = false;
        for (int x = x0; x < x1; x++) {
            boolean dark = false;
            for (int y = y0; y < y1 && !dark; y++) {
                int rgb = img.getRGB(x, y);
                int lum = (((rgb >> 16) & 255) * 299 + ((rgb >> 8) & 255) * 587 + (rgb & 255) * 114) / 1000;
                dark = lum < 140;
            }
            if (dark && !inGlyph) count++;
            inGlyph = dark;
        }
        return count;
    }

    private static int median(int[] values) {
        if (values.length == 0) return 20;
        int[] v = values.clone();
        java.util.Arrays.sort(v);
        return v[v.length / 2];
    }

    private static boolean hasTrainedData(File dir) {
        File[] files = dir.isDirectory() ? dir.listFiles((d, n) -> n.endsWith(".traineddata")) : null;
        return files != null && files.length > 0;
    }
}
