package com.fourunet.pro;

import android.content.Context;

import java.util.regex.Matcher;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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


    static boolean isSystemGeneratedMessage(String body) {
        if (body == null) return false;
        String b = body.trim();
        if (b.isEmpty()) return false;
        return b.contains("فور يو")
                || b.contains("تم التنفيذ")
                || b.contains("تعذر التنفيذ")
                || b.contains("تم الشحن")
                || b.contains("طلب مكرر")
                || b.contains("نفدت كروت")
                || b.contains("عملية تحتاج مراجعة");
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


    static int extractAmountForReview(String body) {
        return extractAmount(body);
    }

    static String extractNameForReview(String body) {
        return extractNameAfterFrom(body);
    }

    static String walletDisplayName(String sender, String body) {
        String hay = ((sender == null ? "" : sender) + " " + (body == null ? "" : body)).toLowerCase(Locale.ROOT);
        if (hay.contains("jawali") || (sender != null && sender.contains("جوالي"))) return "جوالي";
        if (hay.contains("jaib") || (sender != null && sender.contains("جيب"))) return "جيب";
        if (hay.contains("one cash") || hay.contains("onecash") || (sender != null && sender.contains("ون كاش"))) return "ONE Cash";
        if (sender != null && !sender.trim().isEmpty()) return sender.trim();
        return "محفظة";
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
        return parseTrustedCreditRequest(null, body);
    }

    static ParsedCreditRequest parseTrustedCreditRequest(Context context, String body) {
        if (body == null) return null;
        if (isSystemGeneratedMessage(body)) return null;

        // HOTFIX: وحّد الرسالة قبل القراءة. هذا يجعل:
        // 100\n776901570  ==  100 776901570  ==  100-776901570  ==  100.776901570
        // ثم نستخرج رقم العميل أولاً ونحذفه قبل استخراج الفئات حتى لا تُقرأ أرقام الجوال كفئات.
        String normalized = normalizeTrustedRequestText(normalizeDigitsInText(body));
        if (normalized.trim().isEmpty()) return null;

        ArrayList<String> phones = extractLocalPhones(normalized);
        HashSet<Integer> validAmounts = new HashSet<>();
        if (context != null) {
            for (CategoryItem item : AppStore.loadCategories(context)) {
                if (item != null && item.active && item.amount > 0) validAmounts.add(item.amount);
            }
        }
        if (validAmounts.isEmpty()) {
            int[] defaults = new int[]{50,100,150,200,250,300,500,1000,3000,5000,10000};
            for (int a : defaults) validAmounts.add(a);
        }

        String withoutPhones = removeDetectedPhones(normalized, phones);
        withoutPhones = normalizeSeparatorsForNumbers(withoutPhones);

        ArrayList<Integer> amounts = new ArrayList<>();
        ArrayList<Integer> unknownNumbers = new ArrayList<>();
        Matcher num = Pattern.compile("\\d+(?:[\\.,]\\d+)?").matcher(withoutPhones);
        while (num.find()) {
            int value = toIntAmount(num.group());
            if (value <= 0) continue;
            if (validAmounts.contains(value)) amounts.add(value);
            else if (value >= 10) unknownNumbers.add(value);
        }

        boolean hasDigits = Pattern.compile("\\d").matcher(normalized).find();
        if (!hasDigits) return null;

        String reason = "";
        if (phones.size() == 0 && amounts.size() == 0) return null;
        if (phones.size() == 0) reason = "لا يوجد رقم عميل صحيح مكون من 9 أرقام ويبدأ بـ 7.";
        else if (phones.size() > 1) reason = "تم العثور على أكثر من رقم عميل في الطلب؛ يجب إرسال رقم عميل واحد فقط.";
        else if (amounts.size() == 0) reason = "لا توجد فئة كرت مطابقة للباقات المعتمدة في الرسالة.";
        else if (!unknownNumbers.isEmpty()) reason = "توجد أرقام أو فئات غير معتمدة داخل الرسالة: " + joinInts(unknownNumbers);

        int[] arr = new int[amounts.size()];
        for (int i = 0; i < amounts.size(); i++) arr[i] = amounts.get(i);
        String phone = phones.size() == 1 ? phones.get(0) : "";
        String fingerprint = buildTrustedCreditFingerprint(phone, arr);
        return new ParsedCreditRequest(arr, phone, reason, fingerprint);
    }

    private static String normalizeTrustedRequestText(String text) {
        if (text == null) return "";
        String s = text.replace('\u200f', ' ').replace('\u200e', ' ');
        s = s.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
        s = s.replace('ـ', ' ');
        s = s.replace("إلى", " الى ").replace("الى", " الى ");
        s = s.replace("إلي", " الى ").replace("الي", " الى ");
        return s.replaceAll("\\s+", " ").trim();
    }

    private static String normalizeSeparatorsForNumbers(String text) {
        if (text == null) return "";
        // لا نحذف الأرقام؛ فقط نحول الفواصل والرموز التي يستخدمها نقاط البيع بين الفئة والرقم إلى مسافات.
        return text.replace('\n', ' ')
                .replace('\r', ' ')
                .replace('\t', ' ')
                .replace('-', ' ')
                .replace('–', ' ')
                .replace('—', ' ')
                .replace('_', ' ')
                .replace('/', ' ')
                .replace('\\', ' ')
                .replace(':', ' ')
                .replace(';', ' ')
                .replace('|', ' ')
                .replace(',', ' ')
                .replace('،', ' ')
                .replace('.', ' ')
                .replaceAll("\\s+", " ").trim();
    }

    private static String removeDetectedPhones(String text, ArrayList<String> phones) {
        String out = text == null ? "" : text;
        if (phones == null) return out;
        for (String p : phones) {
            if (p == null || p.trim().isEmpty()) continue;
            String phone = p.trim();
            out = out.replace("00967" + phone, " ");
            out = out.replace("967" + phone, " ");
            out = out.replace("0" + phone, " ");
            out = out.replace(phone, " ");
        }
        return out;
    }

    private static String normalizeDigitsInText(String text) {
        if (text == null) return "";
        String ar = "٠١٢٣٤٥٦٧٨٩";
        String fa = "۰۱۲۳۴۵۶۷۸۹";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int idx = ar.indexOf(ch);
            if (idx >= 0) { out.append((char)('0' + idx)); continue; }
            idx = fa.indexOf(ch);
            if (idx >= 0) { out.append((char)('0' + idx)); continue; }
            out.append(ch);
        }
        return out.toString();
    }

    private static ArrayList<String> extractLocalPhones(String text) {
        ArrayList<String> phones = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        Matcher m = Pattern.compile("(?:00967|967|0)?(7\\d{8})(?!\\d)").matcher(text == null ? "" : text);
        while (m.find()) {
            String p = normalizeLocalPhone(m.group(1));
            if (hasValidLocalMobile(p) && !seen.contains(p)) {
                seen.add(p);
                phones.add(p);
            }
        }
        return phones;
    }

    private static String buildTrustedCreditFingerprint(String phone, int[] amounts) {
        ArrayList<Integer> list = new ArrayList<>();
        if (amounts != null) {
            for (int a : amounts) list.add(Math.max(0, a));
        }
        Collections.sort(list);
        return (phone == null ? "" : phone) + "|" + joinInts(list);
    }

    private static String joinInts(ArrayList<Integer> values) {
        if (values == null || values.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    static ParsedPayment parse(Context context, String sender, String body) {
        if (body == null || isSystemGeneratedMessage(body) || isNonPaymentNoise(body)) return null;

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
