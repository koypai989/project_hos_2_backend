package org.hospitalDonation.v1.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

// สร้างใบอนุโมทนาบัตร (PNG) ด้วย Java2D — วางข้อความทับเทมเพลต (ถ้ามี)
@Service
public class CertificateService {

    private final String CERT_DIR = System.getProperty("user.dir") + "/certificate/";
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    private static final Color BG = new Color(0xFDF3D7);
    private static final Color GOLD = new Color(0xB8942F);
    private static final Color INK = new Color(0x4A3B12);

    // คืน path ของไฟล์ใบเซอร์ (เช่น /certificates/cert_123.png) หรือ null ถ้าสร้างไม่ได้
    public String generate(String donorName, double amount, String orgName,
                           String signatureName, String signatureImagePath,
                           boolean isFoundation, LocalDateTime date) {
        try {
            BufferedImage template = loadTemplate();
            final int W = template != null ? template.getWidth() : 1200;
            final int H = template != null ? template.getHeight() : 850;

            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (template != null) {
                g.drawImage(template, 0, 0, W, H, null);
            } else {
                // fallback: พื้นหลัง + กรอบทองวาดเอง
                g.setColor(BG);
                g.fillRect(0, 0, W, H);
                g.setColor(GOLD);
                g.setStroke(new BasicStroke(8));
                g.drawRect(30, 30, W - 60, H - 60);
                g.setStroke(new BasicStroke(2));
                g.drawRect(48, 48, W - 96, H - 96);
            }

            String family = pickThaiFont();
            g.setColor(INK);

            String amountText = formatAmount(amount);
            String words = bahtText(amount);
            String position = (isFoundation ? "ประธาน" : "ผู้อำนวยการ") + safe(orgName);
            String sigName = (signatureName == null || signatureName.isBlank()) ? "ผู้มีอำนาจลงนาม" : signatureName;

            // ตำแหน่ง/ขนาดคำนวณจากความสูงภาพ เพื่อให้เข้ากับเทมเพลตทุกขนาด
            drawCentered(g, "ใบอนุโมทนาบัตร", font(family, Font.BOLD, H * 0.058), W, (int) (H * 0.170));
            drawCentered(g, safe(orgName) + "  ขอขอบพระคุณ", font(family, Font.BOLD, H * 0.032), W, (int) (H * 0.245));
            drawCentered(g, "คุณ" + safe(donorName), font(family, Font.BOLD, H * 0.044), W, (int) (H * 0.350));
            drawCentered(g, "ซึ่งได้บริจาคเงิน " + amountText + " บาท (" + words + ")",
                    font(family, Font.PLAIN, H * 0.028), W, (int) (H * 0.430));
            drawCentered(g, "ให้แก่" + safe(orgName), font(family, Font.PLAIN, H * 0.028), W, (int) (H * 0.480));
            drawCentered(g, "ขอให้ท่านและครอบครัวประสบแต่ความสุขความเจริญตลอดไป",
                    font(family, Font.PLAIN, H * 0.026), W, (int) (H * 0.575));

            // ลายเซ็น (ถ้ามี)
            drawSignatureImage(g, signatureImagePath, W / 2, (int) (H * 0.630),
                    (int) (W * 0.18), (int) (H * 0.11));

            drawCentered(g, "(" + sigName + ")", font(family, Font.PLAIN, H * 0.026), W, (int) (H * 0.820));
            drawCentered(g, position, font(family, Font.PLAIN, H * 0.026), W, (int) (H * 0.865));
            drawCentered(g, "วันที่ " + thaiDate(date), font(family, Font.PLAIN, H * 0.024), W, (int) (H * 0.925));

            g.dispose();

            Files.createDirectories(Paths.get(CERT_DIR));
            String fileName = "cert_" + System.currentTimeMillis() + ".png";
            ImageIO.write(img, "png", new File(CERT_DIR + fileName));
            return "/certificates/" + fileName;
        } catch (Exception e) {
            System.out.println("Certificate generation failed: " + e.getMessage());
            return null;
        }
    }

    // โหลดเทมเพลต: 1) ไฟล์ข้างๆ working dir  2) classpath  (ไม่มีก็คืน null)
    private BufferedImage loadTemplate() {
        try {
            File f = new File(System.getProperty("user.dir") + "/certificate_template.png");
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception ignored) {
        }
        try (InputStream in = getClass().getResourceAsStream("/certificate_template.png")) {
            if (in != null) return ImageIO.read(in);
        } catch (Exception ignored) {
        }
        return null;
    }

    private Font font(String family, int style, double size) {
        return new Font(family, style, (int) Math.round(size));
    }

    private void drawSignatureImage(Graphics2D g, String path, int centerX, int topY, int maxW, int maxH) {
        if (path == null || path.isBlank()) return;
        try {
            String fileName = path.startsWith("/images/") ? path.substring("/images/".length()) : path;
            File f = new File(UPLOAD_DIR + fileName);
            if (!f.exists()) return;
            BufferedImage sig = ImageIO.read(f);
            if (sig == null) return;
            double scale = Math.min((double) maxW / sig.getWidth(), (double) maxH / sig.getHeight());
            int w = (int) (sig.getWidth() * scale);
            int h = (int) (sig.getHeight() * scale);
            g.drawImage(sig, centerX - w / 2, topY, w, h, null);
        } catch (Exception ignored) {
        }
    }

    private void drawCentered(Graphics2D g, String text, Font font, int width, int baselineY) {
        // แปลงเลขอารบิกทั้งหมดเป็นเลขไทยก่อนวาด (ให้ทั้งใบเป็นเลขไทย)
        final String thaiText = toThaiDigits(text);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int x = (width - fm.stringWidth(thaiText)) / 2;
        g.drawString(thaiText, x, baselineY);
    }

    // เลือกฟอนต์ที่แสดงภาษาไทยได้
    private String pickThaiFont() {
        List<String> candidates = Arrays.asList(
                "TH Sarabun New", "Leelawadee UI", "Leelawadee",
                "Angsana New", "Cordia New", "Tahoma", "Microsoft Sans Serif");
        for (String name : candidates) {
            Font f = new Font(name, Font.PLAIN, 20);
            if (f.getFamily().equalsIgnoreCase(name) && f.canDisplay('ก')) { // 'ก'
                return name;
            }
        }
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (new Font(name, Font.PLAIN, 20).canDisplay('ก')) return name;
        }
        return Font.SANS_SERIF;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatAmount(double amount) {
        long baht = (long) amount;
        return String.format("%,d", baht);
    }

    // ---- แปลงวันที่เป็นไทย (เลขไทย + เดือนไทย + พ.ศ.) ----
    private static final String[] THAI_MONTHS = {"", "มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน",
            "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม"};

    private String thaiDate(LocalDateTime date) {
        LocalDateTime d = date == null ? LocalDateTime.now() : date;
        int day = d.getDayOfMonth();
        int month = d.getMonthValue();
        int yearBE = d.getYear() + 543;
        return toThaiDigits(day) + " " + THAI_MONTHS[month] + " " + toThaiDigits(yearBE);
    }

    private static final char[] THAI_DIGITS = {'๐', '๑', '๒', '๓', '๔', '๕', '๖', '๗', '๘', '๙'};

    private String toThaiDigits(int n) {
        return toThaiDigits(String.valueOf(n));
    }

    // แปลงเฉพาะตัวเลข 0-9 เป็นเลขไทย อักขระอื่น (เช่น "," หรือตัวหนังสือ) คงไว้เหมือนเดิม
    private String toThaiDigits(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            sb.append(c >= '0' && c <= '9' ? THAI_DIGITS[c - '0'] : c);
        }
        return sb.toString();
    }

    // ---- แปลงจำนวนเงินเป็นข้อความบาทไทย ----
    private static final String[] DIGITS = {"", "หนึ่ง", "สอง", "สาม", "สี่", "ห้า", "หก", "เจ็ด", "แปด", "เก้า"};
    private static final String[] UNITS = {"", "สิบ", "ร้อย", "พัน", "หมื่น", "แสน"};

    private String bahtText(double amount) {
        long baht = (long) Math.floor(amount);
        long satang = Math.round((amount - baht) * 100);
        StringBuilder sb = new StringBuilder();
        if (baht == 0 && satang == 0) return "ศูนย์บาทถ้วน";
        if (baht > 0) sb.append(readNumber(baht)).append("บาท");
        if (satang == 0) {
            sb.append("ถ้วน");
        } else {
            sb.append(readNumber(satang)).append("สตางค์");
        }
        return sb.toString();
    }

    private String readNumber(long n) {
        if (n == 0) return "";
        if (n >= 1_000_000) {
            long high = n / 1_000_000;
            long low = n % 1_000_000;
            String result = readNumber(high) + "ล้าน";
            if (low > 0) result += readNumber(low);
            return result;
        }
        StringBuilder sb = new StringBuilder();
        String num = String.valueOf(n);
        int len = num.length();
        for (int i = 0; i < len; i++) {
            int d = num.charAt(i) - '0';
            int pos = len - i - 1; // 0=หน่วย,1=สิบ,...
            if (d == 0) continue;
            if (pos == 1 && d == 1) {
                sb.append(UNITS[pos]); // สิบ
            } else if (pos == 1 && d == 2) {
                sb.append("ยี่").append(UNITS[pos]); // ยี่สิบ
            } else if (pos == 0 && d == 1 && len > 1) {
                sb.append("เอ็ด");
            } else {
                sb.append(DIGITS[d]).append(UNITS[pos]);
            }
        }
        return sb.toString();
    }
}
