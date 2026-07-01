package com.fourunet.pro;

class CategoryItem {
    String id;
    int amount;
    String name;
    boolean active;
    long createdAt;

    CategoryItem(String id, int amount, String name, boolean active, long createdAt) {
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.active = active;
        this.createdAt = createdAt;
    }
}

class CardItem {
    String id;
    int amount;
    String code;
    boolean sold;
    String buyerPhone;
    String soldAt;
    String source;
    String createdAt;

    CardItem(String id, int amount, String code, boolean sold, String buyerPhone, String soldAt, String source, String createdAt) {
        this.id = id;
        this.amount = amount;
        this.code = code;
        this.sold = sold;
        this.buyerPhone = buyerPhone;
        this.soldAt = soldAt;
        this.source = source;
        this.createdAt = createdAt == null ? "" : createdAt;
    }
}

class OperationLog {
    String id;
    String provider;
    String sender;
    String customerName;
    String customerPhone;
    int amount;
    String status;
    String message;
    String cardCode;
    String createdAt;

    OperationLog(String id, String provider, String sender, String customerName, String customerPhone, int amount, String status, String message, String cardCode, String createdAt) {
        this.id = id;
        this.provider = provider;
        this.sender = sender;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.amount = amount;
        this.status = status;
        this.message = message;
        this.cardCode = cardCode;
        this.createdAt = createdAt;
    }
}

class ParsedPayment {
    String provider;
    int amount;
    String customerPhone;      // رقم الاستلام النهائي إن كان معروفاً وآمناً
    String customerName;
    String walletIdentity;     // رقم المحفظة / رقم الحساب / معرف الدفع كما ورد في رسالة المحفظة
    String walletIdentityType; // phone / account / name / unknown

    ParsedPayment(String provider, int amount, String customerPhone, String customerName) {
        this(provider, amount, customerPhone, customerName, "", "unknown");
    }

    ParsedPayment(String provider, int amount, String customerPhone, String customerName, String walletIdentity, String walletIdentityType) {
        this.provider = provider == null ? "" : provider;
        this.amount = amount;
        this.customerPhone = customerPhone == null ? "" : customerPhone;
        this.customerName = customerName == null ? "" : customerName;
        this.walletIdentity = walletIdentity == null ? "" : walletIdentity.trim();
        this.walletIdentityType = walletIdentityType == null ? "unknown" : walletIdentityType.trim();
    }
}

class TrustedContact {
    String id;
    String walletName;
    String senderKeywords;
    String fullName;
    String tripleName;
    String phone;              // رقم استلام الكرت / إشعار السداد
    String walletIdentity;     // رقم المحفظة أو رقم الحساب إن وجد
    String identityType;       // name / phone / account / smart
    boolean autoCardAllowed;   // لا يرسل كرت تلقائياً إلا إذا فُعل صراحة
    boolean active;

    TrustedContact(String id, String walletName, String senderKeywords, String fullName, String tripleName, String phone, boolean active) {
        this(id, walletName, senderKeywords, fullName, tripleName, phone, "", "name", false, active);
    }

    TrustedContact(String id, String walletName, String senderKeywords, String fullName, String tripleName, String phone, String walletIdentity, String identityType, boolean autoCardAllowed, boolean active) {
        this.id = id;
        this.walletName = walletName == null || walletName.trim().isEmpty() ? "ONE Cash" : walletName.trim();
        this.senderKeywords = senderKeywords == null ? "" : senderKeywords.trim();
        this.fullName = fullName == null ? "" : fullName.trim();
        this.tripleName = tripleName == null ? "" : tripleName.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.walletIdentity = walletIdentity == null ? "" : walletIdentity.trim();
        this.identityType = identityType == null || identityType.trim().isEmpty() ? (this.walletIdentity.isEmpty() ? "name" : "smart") : identityType.trim();
        this.autoCardAllowed = autoCardAllowed;
        this.active = active;
    }
}

class WalletIdentityResolution {
    boolean approved;
    TrustedContact contact;
    String receiverPhone;
    String displayName;
    String reason;
    boolean autoCardAllowed;

    WalletIdentityResolution(boolean approved, TrustedContact contact, String receiverPhone, String displayName, String reason, boolean autoCardAllowed) {
        this.approved = approved;
        this.contact = contact;
        this.receiverPhone = receiverPhone == null ? "" : receiverPhone;
        this.displayName = displayName == null ? "" : displayName;
        this.reason = reason == null ? "" : reason;
        this.autoCardAllowed = autoCardAllowed;
    }
}

class PosOutlet {
    String id;
    String name;
    String phone;
    String requestCode;
    String notes;
    boolean active;
    boolean rewardsEnabled;
    long createdAt;

    PosOutlet(String id, String name, String phone, String requestCode, String notes, boolean active, boolean rewardsEnabled, long createdAt) {
        this.id = id;
        this.name = name == null ? "" : name.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.requestCode = requestCode == null ? "" : requestCode.trim();
        this.notes = notes == null ? "" : notes.trim();
        this.active = active;
        this.rewardsEnabled = rewardsEnabled;
        this.createdAt = createdAt;
    }
}


class CustomerReward {
    String phone;
    String name;
    int points;
    int totalPaid;
    int totalEarnedPoints;
    int totalUsedPoints;
    int rewardCards;
    String lastPurchaseAt;

    CustomerReward(String phone, String name, int points, int totalPaid, int totalEarnedPoints, int totalUsedPoints, int rewardCards, String lastPurchaseAt) {
        this.phone = phone == null ? "" : phone.trim();
        this.name = name == null ? "" : name.trim();
        this.points = Math.max(0, points);
        this.totalPaid = Math.max(0, totalPaid);
        this.totalEarnedPoints = Math.max(0, totalEarnedPoints);
        this.totalUsedPoints = Math.max(0, totalUsedPoints);
        this.rewardCards = Math.max(0, rewardCards);
        this.lastPurchaseAt = lastPurchaseAt == null ? "" : lastPurchaseAt.trim();
    }
}


class RewardExpiryInfo {
    String phone;
    String name;
    int points;
    int totalPaid;
    int totalEarnedPoints;
    int totalUsedPoints;
    int rewardCards;
    String lastPurchaseAt;
    String expiryAt;
    int daysLeft;

    RewardExpiryInfo(String phone, String name, int points, int totalPaid, int totalEarnedPoints, int totalUsedPoints, int rewardCards, String lastPurchaseAt, String expiryAt, int daysLeft) {
        this.phone = phone == null ? "" : phone.trim();
        this.name = name == null ? "" : name.trim();
        this.points = Math.max(0, points);
        this.totalPaid = Math.max(0, totalPaid);
        this.totalEarnedPoints = Math.max(0, totalEarnedPoints);
        this.totalUsedPoints = Math.max(0, totalUsedPoints);
        this.rewardCards = Math.max(0, rewardCards);
        this.lastPurchaseAt = lastPurchaseAt == null ? "" : lastPurchaseAt.trim();
        this.expiryAt = expiryAt == null ? "" : expiryAt.trim();
        this.daysLeft = Math.max(0, daysLeft);
    }
}

class RewardResult {
    String customerMessagePart;
    String internalNote;
    String rewardCardCode;
    int pointsAdded;
    int pointsBalance;
    boolean rewardSent;
    boolean rewardPending;

    RewardResult(String customerMessagePart, String internalNote, String rewardCardCode, int pointsAdded, int pointsBalance, boolean rewardSent, boolean rewardPending) {
        this.customerMessagePart = customerMessagePart == null ? "" : customerMessagePart;
        this.internalNote = internalNote == null ? "" : internalNote;
        this.rewardCardCode = rewardCardCode == null ? "" : rewardCardCode;
        this.pointsAdded = Math.max(0, pointsAdded);
        this.pointsBalance = Math.max(0, pointsBalance);
        this.rewardSent = rewardSent;
        this.rewardPending = rewardPending;
    }
}


class TrustedCreditAgent {
    String id;
    String name;
    String senderPhone;
    String secondaryPhone;
    String posSignature;
    int creditLimit;
    int usedAmount;
    boolean active;
    boolean rewardsEnabled;
    boolean signatureRequired;
    long createdAt;

    TrustedCreditAgent(String id, String name, String senderPhone, int creditLimit, int usedAmount, boolean active, boolean rewardsEnabled, long createdAt) {
        this(id, name, senderPhone, "", "", creditLimit, usedAmount, active, rewardsEnabled, true, createdAt);
    }

    TrustedCreditAgent(String id, String name, String senderPhone, String posSignature, int creditLimit, int usedAmount, boolean active, boolean rewardsEnabled, boolean signatureRequired, long createdAt) {
        this(id, name, senderPhone, "", posSignature, creditLimit, usedAmount, active, rewardsEnabled, signatureRequired, createdAt);
    }

    TrustedCreditAgent(String id, String name, String senderPhone, String secondaryPhone, String posSignature, int creditLimit, int usedAmount, boolean active, boolean rewardsEnabled, boolean signatureRequired, long createdAt) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name.trim();
        this.senderPhone = senderPhone == null ? "" : senderPhone.trim();
        this.secondaryPhone = secondaryPhone == null ? "" : secondaryPhone.trim();
        this.posSignature = posSignature == null ? "" : posSignature.trim();
        this.creditLimit = Math.max(0, creditLimit);
        this.usedAmount = Math.max(0, usedAmount);
        this.active = active;
        this.rewardsEnabled = rewardsEnabled;
        this.signatureRequired = signatureRequired;
        this.createdAt = createdAt;
    }

    int remaining() {
        return Math.max(0, creditLimit - usedAmount);
    }
}

class ParsedCreditRequest {
    int amount;
    String customerPhone;
    int[] amounts;
    String rejectReason;
    String fingerprint;

    ParsedCreditRequest(int amount, String customerPhone) {
        this(new int[]{Math.max(0, amount)}, customerPhone, "", "");
    }

    ParsedCreditRequest(int[] amounts, String customerPhone, String rejectReason, String fingerprint) {
        this.amounts = amounts == null ? new int[0] : amounts;
        this.customerPhone = customerPhone == null ? "" : customerPhone.trim();
        this.rejectReason = rejectReason == null ? "" : rejectReason.trim();
        this.fingerprint = fingerprint == null ? "" : fingerprint.trim();
        this.amount = totalAmount();
    }

    boolean isValid() {
        return rejectReason == null || rejectReason.trim().isEmpty();
    }

    int totalAmount() {
        int total = 0;
        if (amounts != null) {
            for (int a : amounts) total += Math.max(0, a);
        }
        return total;
    }

    int countCards() {
        return amounts == null ? 0 : amounts.length;
    }

    String amountsText() {
        if (amounts == null || amounts.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < amounts.length; i++) {
            if (i > 0) sb.append(" + ");
            sb.append(amounts[i]);
        }
        return sb.toString();
    }
}


class LedgerCustomer {
    static final String MODE_AUTO_FULL = "auto_full";       // آلي كامل: سلفة + سداد + رصيد
    static final String MODE_SETTLE_ONLY = "settle_only";   // سداد فقط / إيقاف السلف
    static final String MODE_STOPPED = "stopped";           // موقوف

    String id;
    String name;
    String phone;
    boolean trusted;
    boolean loanEnabled;
    int loanLimit;
    int debt;
    int creditBalance;
    String mode;
    boolean active;
    String notes;
    long createdAt;

    LedgerCustomer(String id, String name, String phone, boolean trusted, boolean loanEnabled, int loanLimit, int debt, boolean active, String notes, long createdAt) {
        this(id, name, phone, trusted, loanEnabled, loanLimit, debt, 0, deriveMode(trusted, loanEnabled, active, ""), active, notes, longCreatedAt(createdAt));
    }

    LedgerCustomer(String id, String name, String phone, boolean trusted, boolean loanEnabled, int loanLimit, int debt, int creditBalance, String mode, boolean active, String notes, long createdAt) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.trusted = trusted;
        this.loanEnabled = loanEnabled;
        this.loanLimit = Math.max(0, loanLimit);
        this.debt = Math.max(0, debt);
        this.creditBalance = Math.max(0, creditBalance);
        this.mode = sanitizeMode(deriveMode(trusted, loanEnabled, active, mode));
        this.active = active;
        this.notes = notes == null ? "" : notes.trim();
        this.createdAt = createdAt;
        syncFlagsFromMode();
    }

    private static long longCreatedAt(long createdAt) {
        return createdAt <= 0 ? System.currentTimeMillis() : createdAt;
    }

    static String sanitizeMode(String value) {
        String v = value == null ? "" : value.trim();
        if (MODE_AUTO_FULL.equals(v) || MODE_SETTLE_ONLY.equals(v) || MODE_STOPPED.equals(v)) return v;
        return MODE_SETTLE_ONLY;
    }

    static String deriveMode(boolean trusted, boolean loanEnabled, boolean active, String savedMode) {
        String v = savedMode == null ? "" : savedMode.trim();
        if (MODE_AUTO_FULL.equals(v) || MODE_SETTLE_ONLY.equals(v) || MODE_STOPPED.equals(v)) return v;
        if (!active) return MODE_STOPPED;
        if (trusted && loanEnabled) return MODE_AUTO_FULL;
        return MODE_SETTLE_ONLY;
    }

    void syncFlagsFromMode() {
        String m = effectiveMode();
        if (MODE_AUTO_FULL.equals(m)) {
            active = true;
            trusted = true;
            loanEnabled = true;
        } else if (MODE_SETTLE_ONLY.equals(m)) {
            active = true;
            trusted = true;
            loanEnabled = false;
        } else {
            active = false;
            trusted = false;
            loanEnabled = false;
        }
    }

    String effectiveMode() {
        return sanitizeMode(mode);
    }

    boolean isAutoFull() {
        return active && MODE_AUTO_FULL.equals(effectiveMode());
    }

    boolean isSettleOnly() {
        return active && MODE_SETTLE_ONLY.equals(effectiveMode());
    }

    boolean isStopped() {
        return !active || MODE_STOPPED.equals(effectiveMode());
    }

    boolean canAutoLoan() {
        return active && trusted && loanEnabled && MODE_AUTO_FULL.equals(effectiveMode());
    }

    int remainingLoan() {
        return Math.max(0, loanLimit - debt);
    }

    int availableForRequest() {
        return remainingLoan() + Math.max(0, creditBalance);
    }
}

class LedgerEntry {
    String id;
    String customerId;
    String customerName;
    String customerPhone;
    String type;
    int debit;
    int credit;
    int balanceAfter;
    String description;
    String reference;
    String createdAt;
    String status;
    String paymentMethod;
    int count;

    LedgerEntry(String id, String customerId, String customerName, String customerPhone, String type, int debit, int credit, int balanceAfter, String description, String reference, String createdAt) {
        this(id, customerId, customerName, customerPhone, type, debit, credit, balanceAfter, description, reference, createdAt,
                inferStatus(type), inferPaymentMethod(type, reference), 1);
    }

    LedgerEntry(String id, String customerId, String customerName, String customerPhone, String type, int debit, int credit, int balanceAfter, String description, String reference, String createdAt, String status, String paymentMethod, int count) {
        this.id = id == null ? "" : id;
        this.customerId = customerId == null ? "" : customerId;
        this.customerName = customerName == null ? "" : customerName.trim();
        this.customerPhone = customerPhone == null ? "" : customerPhone.trim();
        this.type = type == null ? "" : type.trim();
        this.debit = Math.max(0, debit);
        this.credit = Math.max(0, credit);
        this.balanceAfter = Math.max(0, balanceAfter);
        this.description = description == null ? "" : description.trim();
        this.reference = reference == null ? "" : reference.trim();
        this.createdAt = createdAt == null ? "" : createdAt.trim();
        this.status = status == null || status.trim().isEmpty() ? inferStatus(this.type) : status.trim();
        this.paymentMethod = paymentMethod == null || paymentMethod.trim().isEmpty() ? inferPaymentMethod(this.type, this.reference) : paymentMethod.trim();
        this.count = Math.max(1, count);
    }

    static String inferStatus(String type) {
        String t = type == null ? "" : type;
        if (t.contains("رفض") || t.contains("مرفوض") || t.contains("فشل")) return "مرفوض";
        if (t.contains("معلق") || t.contains("مراجعة")) return "معلق";
        if (t.contains("سداد")) return "سداد";
        if (t.contains("سلفة")) return "سلفة";
        return "تم";
    }

    static String inferPaymentMethod(String type, String reference) {
        String r = reference == null ? "" : reference.trim();
        String t = type == null ? "" : type;
        if (r.contains("ون") || r.toLowerCase().contains("one")) return "ون كاش";
        if (r.contains("جوالي")) return "جوالي";
        if (r.contains("كريمي")) return "كريمي";
        if (r.contains("فلوسك")) return "فلوسك";
        if (r.contains("جيب")) return "جيب";
        if (r.contains("المحل")) return "عن طريق المحل";
        if (t.contains("سلفة")) return "كرت سلف";
        if (t.contains("سداد")) return r.isEmpty() ? "سداد" : r;
        if (t.contains("قيد")) return r.isEmpty() ? "قيد يدوي" : r;
        return r.isEmpty() ? t : r;
    }
}
