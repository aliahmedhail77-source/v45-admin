package com.fourunet.pro;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.Locale;
import java.util.regex.Pattern;

class PaymentParser {
    static boolean trustedSender(Context context, String sender, String body) {
        if (trustedSender(sender)) return true;
        // المحافظ المضافة يدويًا تُقبل عندما يكون المرسل نفسه ضمن كلمات التعرف، وليس لمجرد أن نص الرسالة يحتوي اسم المحفظة.
        // هذا يمنع الرسائل الخاصة أو المزورة التي يكتب صاحبها نص دفع وهمي.
        return AppStore.matchesAnyWalletSenderKeyword(context, sender);
    }

    static boolean trustedSender(String sender) {
        if (sender == null) return false;
        String s = sender.toLowerCase();
        return s.contains("jawali")
                || s.contains("jaib")
                || s.contains("one cash")
                || s.contains("onecash")
                || sender.contains("جوالي")
                || sender.contains("جيب")
                || sender.contains("ون كاش");
    }

    static boolean looksLikePayment(String body) {
        if (body == null) return false;
        if (isNonPaymentNoise(body)) return false;
        String b = body;
        boolean hasDigits = b.matches(".*\\d+.*");
        if (!hasDigits) return false;
        String upper = b.toUpperCase(Locale.ROOT);
        boolean hasCurrency = b.contains("ريال") || b.contains("ر.ي") || upper.contains("YER");
        boolean hasDepositVerb = b.contains("تم استلام") || b.contains("استلام") || b.contains("تحويل") || b.contains("تم تحويل")
                || b.contains("تم اضافة") || b.contains("تم إضافة") || b.contains("اضيف") || b.contains("أضيف")
                || b.contains("اضافة") || b.contains("إضافة") || b.contains("إيداع") || b.contains("ايداع") || b.contains("مبلغ");
        return hasDepositVerb && hasCurrency;
    }

    static boolean isNonPaymentNoise(String body) {
        if (body == null) return true;
        String b = body.toLowerCase(Locale.ROOT);
        boolean hasRealDepositVerb = b.contains("استلمت") || b.contains("استلام") || b.contains("تم استلام")
                || b.contains("تحويل") || b.contains("تم تحويل") || b.contains("اضيف") || b.contains("أضيف")
                || b.contains("تم اضافة") || b.contains("تم إضافة") || b.contains("ايداع") || b.contains("إيداع");
        if (hasRealDepositVerb) return false;
        String[] noise = new String[]{
                "تعبئة رصيد", "تم تعبئة", "تمت تعبئة", "رصيدك الحالي", "رصيدك هو", "رصيدك:",
                "باقة", "باقتك", "دقائق", "ميجابايت", "gb", "mb", "انترنت", "إنترنت",
                "عرض", "اشترك", "رمز التحقق", "otp", "كود التحقق", "password", "تنبيه أمني", "اعلان", "إعلان"
        };
        for (String n : noise) if (b.contains(n.toLowerCase(Locale.ROOT))) return true;
        return false;
    }

    static boolean hasValidLocalMobile(String phone) {
        String p = normalizeLocalPhone(phone);
        return p.matches("7\\d{8}");
    }

    static String normalizeLocalPhone(String phone) {
        if (phone == null) return "";
        String ar = "٠١٢٣٤٥٦٧٨٩";
        String fa = "۰۱۲۳۴۵۶۷۸۹";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < phone.length(); i++) {
            char ch = phone.charAt(i);
            int idx = ar.indexOf(ch);
            if (idx >= 0) { out.append((char)('0' + idx)); continue; }
            idx = fa.indexOf(ch);
            if (idx >= 0) { out.append((char)('0' + idx)); continue; }
            if (ch >= '0' && ch <= '9') out.append(ch);
        }
        String p = out.toString();
        if (p.startsWith("00967")) p = p.substring(5);
        if (p.startsWith("967")) p = p.substring(3);
        if (p.startsWith("0") && p.length() == 10) p = p.substring(1);
        return p;
    }


    static ParsedCreditRequest parseTrustedCreditRequest(String body) {
        if (body == null) return null;
        Pattern pattern = Pattern.compile("تم\\s*(?:اضافة|إضافة|اضافه|إضافه)\\s+(\\d+(?:[\\.,]\\d+)?)\\s*(?:ريال|ر\\.?ي)?\\s*(?:من|الى|إلى)?\\s*[-:：]?\\s*(\\+?\\d{7,13})", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = pattern.matcher(body);
        if (m.find()) {
            int amount = toIntAmount(m.group(1));
            String phone = normalizeLocalPhone(m.group(2));
            if (amount > 0 && hasValidLocalMobile(phone)) return new ParsedCreditRequest(amount, phone);
        }
        Pattern flexible = Pattern.compile("(?:اضافة|إضافة|اضافه|إضافه)\\s+(\\d+(?:[\\.,]\\d+)?).*?(7\\d{8}|\\d{9,12})", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher mf = flexible.matcher(body);
        if (mf.find()) {
            int amount = toIntAmount(mf.group(1));
            String phone = normalizeLocalPhone(mf.group(2));
            if (amount > 0 && hasValidLocalMobile(phone)) return new ParsedCreditRequest(amount, phone);
        }
        return null;
    }

    static ParsedPayment parse(Context context, String sender, String body) {
        if (body == null || isNonPaymentNoise(body)) return null;

        ParsedPayment p = null;
        if (trustedSender(sender)) {
            p = parseJawali(body);
            if (p != null) return p;

            p = parseJaib(body);
            if (p != null) return p;

            p = parseOneCash(body);
            if (p != null) return p;

            p = parseFallback(sender, body);
            if (p != null) return p;
        }

        p = parseCustomWallet(context, sender, body);
        return p;
    }

    static ParsedPayment parse(String sender, String body) {
        if (!trustedSender(sender) || body == null || isNonPaymentNoise(body)) return null;

        ParsedPayment p = parseJawali(body);
        if (p != null) return p;

        p = parseJaib(body);
        if (p != null) return p;

        p = parseOneCash(body);
        if (p != null) return p;

        return parseFallback(sender, body);
    }

    private static ParsedPayment parseJawali(String body) {
        Pattern pattern = Pattern.compile("استلمت\\s+مبلغ\\s+(\\d+(?:[\\.,]\\d+)?)\\s*YER\\s+من\\s+(\\d+)");
        Matcher m = pattern.matcher(body);
        if (m.find()) {
            int amount = toIntAmount(m.group(1));
            String phone = normalizeLocalPhone(m.group(2));
            if (amount > 0 && hasValidLocalMobile(phone)) return new ParsedPayment("Jawali", amount, phone, "");
        }
        return null;
    }

    private static ParsedPayment parseJaib(String body) {
        Pattern pattern = Pattern.compile("اضيف\\s+(\\d+(?:[\\.,]\\d+)?)\\s*ر\\.?ي.*?من\\s+(.+?)-(\\d+)");
        Matcher m = pattern.matcher(body);
        if (m.find()) {
            int amount = toIntAmount(m.group(1));
            String name = m.group(2).trim();
            String phone = normalizeLocalPhone(m.group(3));
            if (amount > 0 && hasValidLocalMobile(phone)) return new ParsedPayment("Jaib", amount, phone, name);
            if (amount > 0 && !name.isEmpty()) return new ParsedPayment("Jaib", amount, "", name);
        }
        return null;
    }

    private static ParsedPayment parseOneCash(String body) {
        Pattern pattern = Pattern.compile("استلمت\\s+(\\d+(?:[\\.,]\\d+)?).*?\\n\\s*من\\s+(.+?)\\s*\\n\\s*رصيدك", Pattern.DOTALL);
        Matcher m = pattern.matcher(body);
        if (m.find()) {
            int amount = toIntAmount(m.group(1));
            String name = cleanName(m.group(2));
            if (amount > 0 && !name.isEmpty()) return new ParsedPayment("ONE Cash", amount, "", name);
        }

        Pattern flexible = Pattern.compile("استلمت\\s+(\\d+(?:[\\.,]\\d+)?).*?من\\s+(.+?)\\s+رصيدك", Pattern.DOTALL);
        Matcher mf = flexible.matcher(body);
        if (mf.find()) {
            int amount = toIntAmount(mf.group(1));
            String name = cleanName(mf.group(2));
            if (amount > 0 && !name.isEmpty()) return new ParsedPayment("ONE Cash", amount, "", name);
        }
        return null;
    }

    private static ParsedPayment parseCustomWallet(Context context, String sender, String body) {
        if (!AppStore.matchesAnyWalletSenderKeyword(context, sender)) return null;
        int amount = extractAmount(body);
        if (amount <= 0) return null;

        TrustedContact direct = AppStore.findWalletContactInBody(context, sender, body);
        if (direct != null) return new ParsedPayment(direct.walletName, amount, direct.phone, direct.fullName);

        String name = extractNameAfterFrom(body);
        TrustedContact byName = AppStore.findWalletContact(context, name, sender, body);
        if (byName != null) return new ParsedPayment(byName.walletName, amount, byName.phone, byName.fullName);

        if (name != null && !name.trim().isEmpty()) {
            String lowSender = sender == null ? "" : sender.toLowerCase();
            return new ParsedPayment(lowSender.isEmpty() ? "محفظة" : sender, amount, "", cleanName(name));
        }
        return null;
    }

    private static ParsedPayment parseFallback(String sender, String body) {
        int amount = extractAmount(body);
        String phone = "";

        Pattern phonePattern = Pattern.compile("(?:00967|967|0)?(7\\d{8})");
        Matcher mp = phonePattern.matcher(body);
        String last = "";
        while (mp.find()) last = mp.group(1);
        phone = normalizeLocalPhone(last);

        if (amount > 0 && hasValidLocalMobile(phone)) {
            String low = sender == null ? "" : sender.toLowerCase();
            String provider = "Jawali";
            if (low.contains("jaib") || (sender != null && sender.contains("جيب"))) provider = "Jaib";
            if (low.contains("one") || (sender != null && sender.contains("ون كاش"))) provider = "ONE Cash";
            return new ParsedPayment(provider, amount, phone, "");
        }
        return null;
    }

    private static int extractAmount(String body) {
        String[] patterns = new String[]{
                "(?:مبلغ|اضيف|أضيف|إيداع|ايداع|استلمت|استلام|استلم|وصل|تحويل)\\s*(\\d+(?:[\\.,]\\d+)?)",
                "(\\d+(?:[\\.,]\\d+)?)\\s*(?:ريال|ر\\.?ي|YER|YR|Rial)"
        };
        for (String pat : patterns) {
            Matcher m = Pattern.compile(pat, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(body);
            if (m.find()) {
                int amount = toIntAmount(m.group(1));
                if (amount > 0) return amount;
            }
        }
        return 0;
    }

    private static String extractNameAfterFrom(String body) {
        if (body == null) return "";
        Pattern[] patterns = new Pattern[]{
                Pattern.compile("(?:من|From)\\s*[:：-]?\\s*(.+?)(?:\\n|رصيدك|المبلغ|رقم|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
                Pattern.compile("المرسل\\s*[:：-]?\\s*(.+?)(?:\\n|$)", Pattern.DOTALL),
                Pattern.compile("اسم\\s+المرسل\\s*[:：-]?\\s*(.+?)(?:\\n|$)", Pattern.DOTALL)
        };
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(body);
            if (m.find()) return cleanName(m.group(1));
        }
        return "";
    }

    private static String cleanName(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[0-9٠-٩]+", " ")
                .replaceAll("[\\-_:؛;،,]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int toIntAmount(String value) {
        try {
            String v = value.replace(",", ".");
            double d = Double.parseDouble(v);
            return (int) Math.round(d);
        } catch (Exception e) {
            return 0;
        }
    }
}
