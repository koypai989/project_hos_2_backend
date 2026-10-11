package org.hospitalDonation.v1.service.slip;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// แยกข้อมูลธุรกรรมจากข้อความที่ OCR อ่านได้ (ออกแบบตามเลย์เอาต์สลิปธนาคารไทยทั่วไป):
//   หัวสลิป (โอนเงินสำเร็จ / วันเวลา)
//   ผู้โอน: ชื่อ → ธนาคาร → เลขบัญชีปิดบางส่วน (xxx-x-x1234-x)
//   ผู้รับ: ชื่อ → ธนาคาร/เลขผู้รับ → เลขบัญชีปิดบางส่วน
//   จำนวน: 1,234.00 บาท  /  ค่าธรรมเนียม  /  เลขที่รายการ
public final class SlipTextParser {

    public record Result(Double amount, String dateIso, String senderName, String senderId,
                         String receiverName, String receiverId, String receiverBank) {}

    private static final Pattern MONEY = Pattern.compile("(?<![\\d.,])(\\d{1,3}(?:,\\d{3})+|\\d+)\\.(\\d{2})(?![\\d])");
    // เลขบัญชีที่ถูกปิดบางส่วน เช่น xxx-x-x9422-x, XXXXXXXXX6144, ***-*-*1234-*
    private static final Pattern MASKED_ACCOUNT = Pattern.compile("[xX*•\\d][xX*•\\d-]{6,}");
    // 11 ต.ค. 69 12:58 น. / 11 ต.ค. 2569 - 12:58 / 11 Oct 2026, 12:58
    private static final Pattern DATE = Pattern.compile(
            "(\\d{1,2})\\s*([ก-๙a-zA-Z.]{2,12})\\s*(\\d{2,4})\\s*[,\\-]?\\s*(?:เวลา\\s*)?(\\d{1,2})[:.](\\d{2})");

    private static final Map<String, Integer> MONTHS = new LinkedHashMap<>();
    static {
        String[][] th = {
                {"มค", "มกราคม"}, {"กพ", "กุมภาพันธ์"}, {"มีค", "มีนาคม"}, {"เมย", "เมษายน"},
                {"พค", "พฤษภาคม"}, {"มิย", "มิถุนายน"}, {"กค", "กรกฎาคม"}, {"สค", "สิงหาคม"},
                {"กย", "กันยายน"}, {"ตค", "ตุลาคม"}, {"พย", "พฤศจิกายน"}, {"ธค", "ธันวาคม"}};
        String[] en = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
        for (int i = 0; i < 12; i++) {
            MONTHS.put(th[i][0], i + 1);
            MONTHS.put(th[i][1], i + 1);
            MONTHS.put(en[i], i + 1);
        }
    }

    // ชื่อธนาคาร → รหัสธนาคาร (ใช้รหัสเดียวกับ API เดิม เช่น 004 = กสิกรไทย)
    private static final Map<String, String> BANKS = new LinkedHashMap<>();
    static {
        BANKS.put("กรุงเทพ", "002");
        BANKS.put("กสิกร", "004");
        BANKS.put("กรุงไทย", "006");
        BANKS.put("ทหารไทย", "011");
        BANKS.put("ttb", "011");
        BANKS.put("ไทยพาณิชย์", "014");
        BANKS.put("scb", "014");
        BANKS.put("ซีไอเอ็มบี", "022");
        BANKS.put("ยูโอบี", "024");
        BANKS.put("กรุงศรี", "025");
        BANKS.put("ออมสิน", "030");
        BANKS.put("อาคารสงเคราะห์", "033");
        BANKS.put("ธ.ก.ส", "034");
        BANKS.put("เพื่อการเกษตร", "034");
        BANKS.put("เกียรตินาคิน", "069");
        BANKS.put("ทิสโก้", "067");
        BANKS.put("แลนด์", "073");
    }

    private static final String[] HEADER_WORDS = {"สำเร็จ", "successful", "success"};
    private static final String[] FEE_WORDS = {"ค่าธรรมเนียม", "fee"};
    private static final String[] AMOUNT_WORDS = {"จำนวน", "amount"};

    private SlipTextParser() {}

    public static Result parse(List<String> rawLines, boolean thaiAvailable) {
        List<String> lines = new ArrayList<>();
        for (String l : rawLines) {
            String n = normalize(l);
            if (!n.isBlank()) lines.add(n);
        }

        Double amount = findAmount(lines);
        int dateLine = -1;
        String dateIso = null;
        for (int i = 0; i < lines.size() && dateIso == null; i++) {
            dateIso = parseDate(lines.get(i));
            if (dateIso != null) dateLine = i;
        }

        // ตำแหน่งบรรทัดเลขบัญชีที่ถูกปิดบางส่วน: ตัวแรก = ผู้โอน, ตัวที่สอง = ผู้รับ
        List<Integer> accountLines = new ArrayList<>();
        List<String> accounts = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String acc = findMaskedAccount(lines.get(i));
            if (acc != null) {
                accountLines.add(i);
                accounts.add(acc);
            }
        }
        String senderId = accounts.size() > 0 ? accounts.get(0) : null;
        String receiverId = accounts.size() > 1 ? accounts.get(1) : null;

        String senderName = null;
        String receiverName = null;
        String receiverBank = null;
        // ชื่อภาษาไทยอ่านได้เฉพาะเมื่อมีข้อมูลภาษาไทยของ OCR — ไม่งั้นจะได้ตัวอักษรมั่ว ไม่ใช้ดีกว่า
        if (thaiAvailable && !accountLines.isEmpty()) {
            senderName = findName(lines, dateLine + 1, accountLines.get(0));
            if (accountLines.size() > 1) {
                receiverName = findName(lines, accountLines.get(0) + 1, accountLines.get(1));
                receiverBank = findBank(lines, accountLines.get(0) + 1, accountLines.get(1));
            }
        }

        return new Result(amount, dateIso, senderName, senderId,
                receiverName, receiverId, receiverBank);
    }

    // ---------- ยอดเงิน ----------

    private static Double findAmount(List<String> lines) {
        // 1) บรรทัดที่มีคำว่า "จำนวน" (หรือบรรทัดถัดไป) แต่ไม่ใช่ค่าธรรมเนียม
        for (int i = 0; i < lines.size(); i++) {
            String l = lines.get(i).toLowerCase();
            if (containsAny(l, AMOUNT_WORDS) && !containsAny(l, FEE_WORDS)) {
                for (int j = i; j <= Math.min(i + 2, lines.size() - 1); j++) {
                    if (j > i && containsAny(lines.get(j).toLowerCase(), FEE_WORDS)) break;
                    Double v = money(lines.get(j));
                    if (v != null) return v;
                }
            }
        }
        // 2) อ่านป้ายไม่ออก → ใช้ตัวเลขทศนิยม 2 ตำแหน่งตัวแรก (สลิปทุกธนาคารแสดงยอดก่อนค่าธรรมเนียม)
        boolean afterFee = false;
        for (String line : lines) {
            if (containsAny(line.toLowerCase(), FEE_WORDS)) afterFee = true;
            if (afterFee) continue;
            Double v = money(line);
            if (v != null && v > 0) return v;
        }
        return null;
    }

    private static Double money(String line) {
        Matcher m = MONEY.matcher(line);
        while (m.find()) {
            // ข้ามรูปแบบเวลา/วันที่ เช่น 12.58 ที่ตามด้วย น.
            String after = line.substring(m.end()).trim();
            if (after.startsWith("น.") || after.startsWith("น ")) continue;
            return Double.parseDouble(m.group(1).replace(",", "") + "." + m.group(2));
        }
        return null;
    }

    // ---------- วันเวลา ----------

    private static String parseDate(String line) {
        Matcher m = DATE.matcher(line);
        while (m.find()) {
            Integer month = MONTHS.get(m.group(2).replace(".", "").toLowerCase());
            if (month == null) continue;
            int day = Integer.parseInt(m.group(1));
            int year = Integer.parseInt(m.group(3));
            if (year < 100) year += 2500;      // 69 → 2569 (พ.ศ. 2 หลัก)
            if (year > 2400) year -= 543;      // พ.ศ. → ค.ศ.
            int hour = Integer.parseInt(m.group(4));
            int minute = Integer.parseInt(m.group(5));
            try {
                // เวลาบนสลิปเป็นเวลาไทย → แปลงเป็น UTC รูปแบบเดียวกับ API เดิม (2026-03-17T10:00:00.000Z)
                return LocalDateTime.of(year, month, day, hour, minute)
                        .atZone(ZoneId.of("Asia/Bangkok"))
                        .withZoneSameInstant(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            } catch (RuntimeException ignored) {
                // วันที่ไม่มีจริง (OCR อ่านผิด) → ลองตัวถัดไป
            }
        }
        return null;
    }

    // ---------- ผู้โอน / ผู้รับ ----------

    private static String findMaskedAccount(String line) {
        Matcher m = MASKED_ACCOUNT.matcher(line);
        while (m.find()) {
            String s = m.group();
            long masks = s.chars().filter(c -> c == 'x' || c == 'X' || c == '*' || c == '•').count();
            long digits = s.chars().filter(Character::isDigit).count();
            if (masks >= 3 && digits >= 3) return s;
        }
        return null;
    }

    private static String findName(List<String> lines, int from, int to) {
        for (int i = Math.max(from, 0); i < to; i++) {
            String l = lines.get(i);
            if (isBankLine(l) || containsAny(l.toLowerCase(), HEADER_WORDS)) continue;
            if (parseDate(l) != null) continue;
            String name = cleanName(l);
            if (name != null) return name;
        }
        return null;
    }

    private static String findBank(List<String> lines, int from, int to) {
        for (int i = Math.max(from, 0); i < to; i++) {
            String code = bankCode(lines.get(i));
            if (code != null) return code;
        }
        return null;
    }

    private static boolean isBankLine(String line) {
        return line.startsWith("ธ.") || line.contains("ธนาคาร") || bankCode(line) != null;
    }

    private static String bankCode(String line) {
        String l = line.toLowerCase();
        for (Map.Entry<String, String> e : BANKS.entrySet()) {
            if (l.contains(e.getKey())) return e.getValue();
        }
        return null;
    }

    // ตัดเศษอักขระที่ OCR อ่านจากไอคอน/โลโก้หน้าชื่อ แล้วต้องมีตัวอักษรอย่างน้อย 2 ตัว
    private static String cleanName(String line) {
        String s = line.replaceAll("^[^ก-๙A-Za-z]+", "").replaceAll("[^ก-๙A-Za-z.)]+$", "").trim();
        long letters = s.chars().filter(Character::isLetter).count();
        if (letters < 2) return null;
        // บรรทัดที่เป็นตัวเลขล้วน (เช่น เลขผู้รับชำระ) ไม่ใช่ชื่อ
        if (s.replaceAll("[\\s\\-]", "").matches("\\d+")) return null;
        return s;
    }

    // ---------- util ----------

    private static String normalize(String s) {
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            // เลขไทย ๐-๙ → 0-9
            b.append(c >= '๐' && c <= '๙' ? (char) ('0' + (c - '๐')) : c);
        }
        return b.toString().trim();
    }

    private static boolean containsAny(String s, String[] words) {
        for (String w : words) if (s.contains(w)) return true;
        return false;
    }
}
