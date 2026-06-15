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
    String customerPhone;
    String customerName;

    ParsedPayment(String provider, int amount, String customerPhone, String customerName) {
        this.provider = provider;
        this.amount = amount;
        this.customerPhone = customerPhone;
        this.customerName = customerName;
    }
}

class TrustedContact {
    String id;
    String walletName;
    String senderKeywords;
    String fullName;
    String tripleName;
    String phone;
    boolean active;

    TrustedContact(String id, String walletName, String senderKeywords, String fullName, String tripleName, String phone, boolean active) {
        this.id = id;
        this.walletName = walletName == null || walletName.trim().isEmpty() ? "ONE Cash" : walletName.trim();
        this.senderKeywords = senderKeywords == null ? "" : senderKeywords.trim();
        this.fullName = fullName == null ? "" : fullName.trim();
        this.tripleName = tripleName == null ? "" : tripleName.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.active = active;
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
    int creditLimit;
    int usedAmount;
    boolean active;
    long createdAt;

    TrustedCreditAgent(String id, String name, String senderPhone, int creditLimit, int usedAmount, boolean active, long createdAt) {
        this.id = id == null ? "" : id;
        this.name = name == null ? "" : name.trim();
        this.senderPhone = senderPhone == null ? "" : senderPhone.trim();
        this.creditLimit = Math.max(0, creditLimit);
        this.usedAmount = Math.max(0, usedAmount);
        this.active = active;
        this.createdAt = createdAt;
    }

    int remaining() {
        return Math.max(0, creditLimit - usedAmount);
    }
}

class ParsedCreditRequest {
    int amount;
    String customerPhone;

    ParsedCreditRequest(int amount, String customerPhone) {
        this.amount = Math.max(0, amount);
        this.customerPhone = customerPhone == null ? "" : customerPhone.trim();
    }
}
