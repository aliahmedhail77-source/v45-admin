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
        return knownWalletSender(sender);
    }

    // HOTFIX 14.3.1: قبول مرسلي المحافظ الشائعة حتى لو لم يضف المستخدم اسم المحفظة يدويًا بعد.
    private static boolean knownWalletSender(String sender) {
        if (sender == null) return false;
        String raw = sender.trim();
        if (raw.isEmpty()) return false;
        String s = raw.toLowerCase(Locale.ROOT).replace("-", " ").replace("_", " ");
        return s.contains("jawali")
                || s.contains("jaib")
                || s.contains("one cash")
                || s.contains("onecash")
                || s.contains("onec")
                || s.contains("kuraimi")
                || s.contains("alkuraimi")
                || s.contains("al kuraimi")
                || s.contains("floosk")
                || s.contains("floosak")
                || s.contains("yemen cash")
                || s.contains("yemencash")
                || s.contains("saba cash")
                || s.contains("sabacash")
                || s.contains("beys")
                || s.contains("bais")
                || raw.contains("جوالي")
                || raw.contains("جيب")
                || raw.contains("ون كاش")
                || raw.contains("ونكاش")
                || raw.contains("الكريمي")
                || raw.contains("كريمي")
                || raw.contains("فلوسك")
                || raw.contains("يمن كاش")
                || raw.contains("سبأ كاش")
                || raw.contains("سبا كاش")
                || raw.contains("بيس")
                || raw.contains("محفظ")
                || raw.contains("كاش");
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

    static boolean isIgnoredNotificationMessage(String body) {
        if (body == null) return true;
        String raw = body.trim();
        if (raw.isEmpty()) return true;
        String normalized = normalizeDigitsInText(raw);
        String lower = normalized.toLowerCase(Locale.ROOT);

        // لا نعتبر إيصالات المحافظ ضوضاء حتى لو احتوت كلمة "رصيدك" مثل رسائل ONE Cash.
        if (hasRealDepositVerb(lower)) return false;

        String[] ignored = new String[]{
                "missed call", "you have a missed call", "مكالمة فائتة", "لم يتم الرد", "اتصل بك",
                "voicemail", "voice mail", "رصيدك", "اشعار", "إشعار"
        };
        for (String token : ignored) {
            if (lower.contains(token.toLowerCase(Locale.ROOT))) return true;
        }
        return isDateTimeOnlyMessage(normalized);
    }

    private static boolean hasRealDepositVerb(String text) {
        if (text == null) return false;
        String b = normalizeArabicLetters(text).toLowerCase(Locale.ROOT);
        return b.contains("استلمت") || b.contains("استلم") || b.contains("استلام") || b.contains("تم استلام")
                || b.contains("تحويل") || b.contains("تم تحويل") || b.contains("حواله") || b.contains("حوالة")
                || b.contains("اضيف") || b.contains("تم اضافه") || b.contains("اضافه")
                || b.contains("ايداع") || b.contains("اودع") || b.contains("تم ايداع")
                || b.contains("سداد") || b.contains("تسديد") || b.contains("سدد")
                || b.contains("دفع") || b.contains("دفعت") || b.contains("تم دفع")
                || b.contains("ارسل") || b.contains("ارسال") || b.contains("تم ارسال")
                || b.contains("استقبلت") || b.contains("وصل") || b.contains("ورد")
                || b.contains("مبلغ");
    }

    private static String normalizeArabicLetters(String text) {
        if (text == null) return "";
        return text.replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
                .replace('ٱ', 'ا').replace('ة', 'ه').replace('ى', 'ي');
    }

    private static boolean isDateTimeOnlyMessage(String body) {
        if (body == null) return false;
        String s = body.trim();
        if (s.isEmpty()) return false;
        boolean hasDateOrTime = Pattern.compile("(?:\\d{1,4}[-/]\\d{1,2}[-/]\\d{1,4}|\\d{1,2}:\\d{2})").matcher(s).find();
        boolean hasLetters = Pattern.compile("[A-Za-z\\p{InArabic}]").matcher(s).find();
        boolean hasDigits = Pattern.compile("\\d").matcher(s).find();
        return hasDigits && hasDateOrTime && !hasLetters;
    }

    static boolean looksLikePayment(String body) {
        if (body == null) return false;
        if (isNonPaymentNoise(body)) return false;
        String b = normalizeDigitsInText(body);
        boolean hasDigits = b.matches(".*\\d+.*");
        if (!hasDigits) return false;
        String upper = b.toUpperCase(Locale.ROOT);
        boolean hasCurrency = b.contains("ريال") || b.contains("ر.ي") || upper.contains("YER") || upper.contains("YR") || upper.contains("RIAL");
        boolean hasDepositVerb = hasRealDepositVerb(b);
        return hasDepositVerb && (hasCurrency || extractAmount(b) > 0);
    }


    static int extractAmountForReview(String body) {
        return extractAmount(body);
    }

    static String extractNameForReview(String body) {
        return extractNameAfterFrom(body);
    }

    static String walletDisplayName(String sender, String body) {
        String hay = ((sender == null ? "" : sender) + " " + (body == null ? "" : body)).toLowerCase(Locale.ROOT);
        String rawSender = sender == null ? "" : sender.trim();
        if (hay.contains("jawali") || rawSender.contains("جوالي")) return "جوالي";
        if (hay.contains("jaib") || rawSender.contains("جيب")) return "جيب";
        if (hay.contains("one cash") || hay.contains("onecash") || rawSender.contains("ون كاش") || rawSender.contains("ونكاش")) return "ONE Cash";
        if (hay.contains("kuraimi") || hay.contains("alkuraimi") || rawSender.contains("الكريمي") || rawSender.contains("كريمي")) return "كريمي";
        if (hay.contains("floosk") || hay.contains("floosak") || rawSender.contains("فلوسك")) return "فلوسك";
        if (hay.contains("yemen cash") || hay.contains("yemencash") || rawSender.contains("يمن كاش")) return "يمن كاش";
        if (hay.contains("saba cash") || hay.contains("sabacash") || rawSender.contains("سبأ كاش") || rawSender.contains("سبا كاش")) return "سبأ كاش";
        if (rawSender.contains("بيس") || hay.contains("beys") || hay.contains("bais")) return "بيس";
        if (!rawSender.isEmpty()) return rawSender;
        return "محفظة";
    }

    static boolean isNonPaymentNoise(String body) {
        if (body == null) return true;
        String b = normalizeDigitsInText(body).toLowerCase(Locale.ROOT);
        if (hasRealDepositVerb(b)) return false;
        if (isIgnoredNotificationMessage(body)) return true;
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



    static boolean isTrustedCreditBalanceRequest(String body) {
        if (body == null) return false;
        String b = body.trim().toLowerCase(Locale.ROOT);
        if (b.isEmpty()) return false;
        if (isSystemGeneratedMessage(b) || isIgnoredNotificationMessage(b)) return false;
        b = b.replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا').replace('ى', 'ي').replace('ة', 'ه');
        boolean hasRequest = b.contains("طلب رصيد")
                || b.contains("احتاج رصيد")
                || b.contains("اريد رصيد")
                || b.contains("نريد رصيد")
                || b.contains("عايز رصيد")
                || b.contains("عبئ رصيد")
                || b.contains("عبئ لي")
                || b.contains("اضف رصيد")
                || b.contains("اضافة رصيد")
                || b.contains("رصيد لو سمحت")
                || b.matches(".*(^|\\s)رصيد(\\s|$).*");
        if (!hasRequest) return false;
        // لا تعتبر طلب رصيد إذا كانت رسالة بيع عادية فيها رقم عميل وفئة؛ هذه تذهب لمسار البيع بعد حذف البصمة.
        ParsedCreditRequest maybeSale = parseTrustedCreditRequest(null, body);
        if (maybeSale != null && maybeSale.isValid()) return false;
        return true;
    }

    static ParsedCreditRequest parseTrustedCreditRequest(String body) {
        return parseTrustedCreditRequest(null, body);
    }

    static ParsedCreditRequest parseTrustedCreditRequest(Context context, String body) {
        if (body == null) return null;
        if (isSystemGeneratedMessage(body) || isIgnoredNotificationMessage(body)) return null;

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
        if (body == null || isSystemGeneratedMessage(body) || isIgnoredNotificationMessage(body) || isNonPaymentNoise(body)) return null;
        String normalizedBody = normalizeDigitsInText(body);

        ParsedPayment p = null;
        if (trustedSender(sender)) {
            p = parseJawali(normalizedBody);
            if (p != null) return p;

            p = parseJaib(normalizedBody);
            if (p != null) return p;

            p = parseOneCash(normalizedBody);
            if (p != null) return p;

            p = parseFallback(sender, normalizedBody);
            if (p != null) return p;

            p = parseGenericTrustedWallet(sender, normalizedBody);
            if (p != null) return p;
        }

        p = parseCustomWallet(context, sender, normalizedBody);
        return p;
    }

    static ParsedPayment parse(String sender, String body) {
        if (!trustedSender(sender) || body == null || isSystemGeneratedMessage(body) || isIgnoredNotificationMessage(body) || isNonPaymentNoise(body)) return null;
        String normalizedBody = normalizeDigitsInText(body);

        ParsedPayment p = parseJawali(normalizedBody);
        if (p != null) return p;

        p = parseJaib(normalizedBody);
        if (p != null) return p;

        p = parseOneCash(normalizedBody);
        if (p != null) return p;

        p = parseFallback(sender, normalizedBody);
        if (p != null) return p;

        return parseGenericTrustedWallet(sender, normalizedBody);
    }

    private static ParsedPayment parseJawali(String body) {
        Pattern pattern = Pattern.compile("استلمت\\s+مبلغ\\s+(\\d+(?:[\\.,]\\d+)?)\\s*YER\\s+من\\s+(\\d+)");
        Matcher m = pattern.matcher(body);
        if (m.find()) {
            int amount = toIntAmount(m.group(1));
            String phone = normalizeLocalPhone(m.group(2));
            if (amount > 0 && hasValidLocalMobile(phone)) return new ParsedPayment("Jawali", amount, "", "", phone, "phone");
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
            if (amount > 0 && hasValidLocalMobile(phone)) return new ParsedPayment("Jaib", amount, "", name, phone, "phone");
            if (amount > 0 && !name.isEmpty()) return new ParsedPayment("Jaib", amount, "", name, "", "name");
        }
        return null;
    }

    private static ParsedPayment parseOneCash(String body) {
        String b = normalizeArabicLetters(normalizeDigitsInText(body == null ? "" : body));
        Pattern pattern = Pattern.compile("(?:استلمت|اودعت)\\s+(\\d+(?:[\\.,]\\d+)?).*?\\n\\s*من\\s+(.+?)\\s*\\n\\s*(?:رصيدك|الرسوم|رسوم)", Pattern.DOTALL);
        Matcher m = pattern.matcher(b);
        if (m.find()) {
            int amount = toIntAmount(m.group(1));
            String name = cleanName(m.group(2));
            if (amount > 0 && !name.isEmpty()) return new ParsedPayment("ONE Cash", amount, "", name, "", "name");
        }

        Pattern flexible = Pattern.compile("(?:استلمت|اودعت)\\s+(\\d+(?:[\\.,]\\d+)?).*?من\\s+(.+?)(?:\\s+رصيدك|\\s+الرصيد|\\n\\s*الرسوم|\\n\\s*رسوم|$)", Pattern.DOTALL);
        Matcher mf = flexible.matcher(b);
        if (mf.find()) {
            int amount = toIntAmount(mf.group(1));
            String name = cleanName(mf.group(2));
            if (amount > 0 && !name.isEmpty()) return new ParsedPayment("ONE Cash", amount, "", name, "", "name");
        }
        return null;
    }

    private static ParsedPayment parseCustomWallet(Context context, String sender, String body) {
        if (!AppStore.matchesAnyWalletSenderKeyword(context, sender)) return null;
        int amount = extractAmount(body);
        if (amount <= 0) return null;

        String name = extractNameAfterFrom(body);
        String identity = extractWalletIdentityForReview(body);
        String type = identityTypeFor(identity);
        String provider = walletDisplayName(sender, body);
        if (name != null && !name.trim().isEmpty()) {
            return new ParsedPayment(provider, amount, "", cleanName(name), identity, type);
        }
        if (identity != null && !identity.trim().isEmpty()) {
            return new ParsedPayment(provider, amount, "", "", identity, type);
        }
        return new ParsedPayment(provider, amount, "", "", "", "unknown");
    }

    private static ParsedPayment parseGenericTrustedWallet(String sender, String body) {
        int amount = extractAmount(body);
        if (amount <= 0) return null;
        String phone = "";
        Matcher mp = Pattern.compile("(?:00967|967|0)?(7\\d{8})").matcher(body == null ? "" : body);
        String last = "";
        while (mp.find()) last = mp.group(1);
        phone = normalizeLocalPhone(last);
        String name = extractNameAfterFrom(body);
        String provider = walletDisplayName(sender, body);
        if (hasValidLocalMobile(phone)) return new ParsedPayment(provider, amount, "", name, phone, "phone");
        String identity = extractWalletIdentityForReview(body);
        if (name != null && !name.trim().isEmpty()) return new ParsedPayment(provider, amount, "", name, identity, identityTypeFor(identity));
        return new ParsedPayment(provider, amount, "", "", identity, identityTypeFor(identity));
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
            String provider = walletDisplayName(sender, body);
            return new ParsedPayment(provider, amount, "", "", phone, "phone");
        }
        return null;
    }

    private static int extractAmount(String body) {
        String b = normalizeArabicLetters(normalizeDigitsInText(body == null ? "" : body));
        String[] patterns = new String[]{
                "(?:مبلغ|اضيف|اضافه|ايداع|اودعت|اودع|استلمت|استلام|استلم|وصل|ورد|تحويل|حواله|سداد|تسديد|سدد|دفع|دفعت|ارسل|ارسال)\\s*[:：-]?\\s*(\\d+(?:[\\.,]\\d+)?)",
                "(\\d+(?:[\\.,]\\d+)?)\\s*(?:ريال|ر\\.?ي|YER|YR|Rial)",
                "(?:amount|received|deposit|transfer|payment|paid)\\s*[:：-]?\\s*(\\d+(?:[\\.,]\\d+)?)"
        };
        for (String pat : patterns) {
            Matcher m = Pattern.compile(pat, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(b);
            if (m.find()) {
                int amount = toIntAmount(m.group(1));
                if (amount > 0) return amount;
            }
        }
        return 0;
    }


    static String extractWalletIdentityForReview(String body) {
        if (body == null) return "";
        String b = normalizeDigitsInText(body);
        // رقم هاتف محلي واضح بعد الاسم أو داخل الرسالة
        Matcher phone = Pattern.compile("(?:00967|967|0)?(7\\d{8})").matcher(b);
        String lastPhone = "";
        while (phone.find()) lastPhone = normalizeLocalPhone(phone.group(1));
        if (hasValidLocalMobile(lastPhone)) return lastPhone;

        // رقم حساب / حساب / اكاونت / account
        Matcher account = Pattern.compile("(?:رقم\\s*الحساب|حساب|account|acc|acct)\\s*[:：#-]?\\s*([0-9]{4,20})", Pattern.CASE_INSENSITIVE).matcher(b);
        if (account.find()) return account.group(1).trim();
        return "";
    }

    static String identityTypeFor(String identity) {
        if (identity == null || identity.trim().isEmpty()) return "name";
        String clean = normalizeLocalPhone(identity);
        if (hasValidLocalMobile(clean)) return "phone";
        return "account";
    }

    static String extractNameAfterFrom(String body) {
        if (body == null) return "";
        String b = normalizeDigitsInText(body);
        Pattern[] patterns = new Pattern[]{
                Pattern.compile("(?:من|From|from)\\s*[:：-]?\\s*(.+?)(?:\\n|رصيدك|الرصيد|المبلغ|رقم|هاتف|جوال|رسوم|عمولة|التاريخ|تاريخ|الوقت|وقت|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
                Pattern.compile("(?:المرسل|مرسل|اسم\\s+المرسل|اسم\\s+العميل|العميل)\\s*[:：-]?\\s*(.+?)(?:\\n|رقم|هاتف|جوال|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
                Pattern.compile("(?:حوالة|حواله|ايداع|سداد|دفع).*?(?:من)\\s*[:：-]?\\s*(.+?)(?:\\n|رقم|هاتف|جوال|رصيد|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
        };
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(b);
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
