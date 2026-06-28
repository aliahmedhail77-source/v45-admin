package com.fourunet.pro;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.UUID;

class SmsProcessor {
    static final String ACTION_SMS_SENT = "com.fourunet.pro.SMS_SENT";
    private static final Object SMS_QUEUE_LOCK = new Object();


    private static final ArrayDeque<QueuedSms> TRUSTED_SMS_QUEUE = new ArrayDeque<>();
    private static boolean smsQueueWorkerRunning = false;

    private static class QueuedSms {
        Context context;
        String sender;
        String body;
        long receivedAt;
        String rawLogId;
        QueuedSms(Context context, String sender, String body, long receivedAt, String rawLogId) {
            this.context = context;
            this.sender = sender;
            this.body = body;
            this.receivedAt = receivedAt;
            this.rawLogId = rawLogId;
        }
    }

    static void enqueueIncomingSms(Context context, String sender, String body, long receivedAt) {
        if (context == null || body == null) return;
        long safeReceivedAt = receivedAt <= 0L ? System.currentTimeMillis() : receivedAt;
        final Context appContext = context.getApplicationContext() == null ? context : context.getApplicationContext();
        final String safeSender = sender == null ? "" : sender;
        final String safeBody = body;
        if (PaymentParser.isSystemGeneratedMessage(safeBody) || PaymentParser.isIgnoredNotificationMessage(safeBody)) return;
        if (!isTrustedQueueCandidate(appContext, safeSender, safeBody)) return;
        if (AppStore.isExactPaymentTextProcessed(appContext, safeSender, safeBody)) return;
        final String rawId = addLog(appContext, "SMS موثوق", safeSender, "", "", 0, "في الطابور الموثوق",
                "تم اعتماد الرسالة لأنها من مصدر موثوق، وأضيفت إلى طابور FIFO حسب ترتيب الوصول.\nالنص: " + safeBody, "");
        synchronized (SMS_QUEUE_LOCK) {
            TRUSTED_SMS_QUEUE.add(new QueuedSms(appContext, safeSender, safeBody, safeReceivedAt, rawId));
            if (smsQueueWorkerRunning) return;
            smsQueueWorkerRunning = true;
        }
        new Thread(SmsProcessor::drainTrustedSmsQueue).start();
    }

    private static void drainTrustedSmsQueue() {
        while (true) {
            QueuedSms item;
            synchronized (SMS_QUEUE_LOCK) {
                item = TRUSTED_SMS_QUEUE.poll();
                if (item == null) {
                    smsQueueWorkerRunning = false;
                    return;
                }
            }
            AppStore.updateLogStatus(item.context, item.rawLogId, "قيد المعالجة", "بدأت معالجة الرسالة الموثوقة من الطابور بالترتيب.\nالنص: " + item.body);
            try {
                processIncomingSms(item.context, item.sender, item.body, item.receivedAt);
                AppStore.updateLogStatus(item.context, item.rawLogId, "تمت قراءة الطابور", "انتهت قراءة الرسالة الموثوقة. راجع سجل العملية الناتجة للتنفيذ أو سبب التعليق.\nالنص: " + item.body);
            } catch (Exception ex) {
                AppStore.updateLogStatus(item.context, item.rawLogId, "فشل الطابور", "فشل غير متوقع أثناء معالجة SMS موثوق. السبب: " + ex.getMessage() + "\nالنص: " + item.body);
                NotifyHelper.notifyPendingAction(item.context, item.rawLogId, 0, "", "فشل في طابور SMS الموثوق");
            }
        }
    }

    private static boolean isTrustedQueueCandidate(Context context, String sender, String body) {
        // لا نضيف إلى الطابور إلا الرسائل التي نستطيع قراءتها كعملية مسموحة فعلاً.
        // بهذا يتم تجاهل رسائل المنظمات، العروض، تعبئة الرصيد، الرسائل الخاصة، وأي رسالة مزورة من مرسل غير موجود في المسموح.
        if (PaymentParser.isSystemGeneratedMessage(body) || PaymentParser.isIgnoredNotificationMessage(body)) return false;
        if (PaymentParser.isNonPaymentNoise(body)) return false;

        // HOTFIX STAGE 12.4:
        // في V45_STAGE12.1 كان طلب السلفة الرقمي مثل "100" لا يدخل الطابور أصلاً،
        // لأن فحص الطابور كان يقبل رسائل المحافظ أو نقاط البيع فقط.
        // لذلك كان الزبون الموثوق صاحب السقف يرسل 100 ولا يحدث أي تعامل.
        // نسمح هنا برسائل السلفة الرقمية/البسيطة فقط إذا كان مرسلها مسجلاً في الدفتر
        // كزبون نشط وموثوق والسلفة مفعلة له، ثم تتابع المعالجة داخل processSmartLoanRequest.
        if (AppStore.isSmartLedgerEnabled(context) && AppStore.isLedgerLoanEnabled(context) && AppStore.isLoanRequestText(body)) {
            LedgerCustomer loanCustomer = AppStore.findLedgerCustomerByPhone(context, sender);
            if (loanCustomer != null) return true;
        }

        TrustedCreditAgent queueAgent = AppStore.findTrustedCreditSender(context, sender);
        if (queueAgent != null) {
            String signedBody = AppStore.stripTrustedCreditSignature(queueAgent, body);
            return signedBody != null && (PaymentParser.isTrustedCreditBalanceRequest(signedBody) || PaymentParser.parseTrustedCreditRequest(context, signedBody) != null);
        }
        if (PaymentParser.parse(context, sender, body) != null) return true;
        return PaymentParser.looksLikePayment(body);
    }

    static void processIncomingSms(Context context, String sender, String body) {
        processIncomingSms(context, sender, body, System.currentTimeMillis());
    }

    static void processIncomingSms(Context context, String sender, String body, long receivedAt) {
        if (context == null || body == null) return;
        // STAGE 10.2: لا تعالج رسائل النظام الصادرة كطلبات واردة حتى لو احتوت أرقاماً أو فئات.
        if (PaymentParser.isSystemGeneratedMessage(body) || PaymentParser.isIgnoredNotificationMessage(body)) return;

        // Deduplicate the same SMS PDU only, not repeated identical requests.
        // Old versions used sender+body only, so repeated messages like
        // "تم إضافة 100 من-776901570" were ignored after the first one.
        // Including the SMS timestamp lets each real incoming SMS be processed,
        // while repeated delivery of the same SMS broadcast stays protected.
        if (receivedAt <= 0L) receivedAt = System.currentTimeMillis();
        String eventId = sha256((sender == null ? "" : sender) + "|" + receivedAt + "|" + body);
        if (AppStore.isProcessed(context, eventId)) return;

        // STAGE 12: الدفتر المحاسبي الذكي - طلبات السلفة من الزبائن الموثوقين فقط.
        if (AppStore.isSmartLedgerEnabled(context) && AppStore.isLedgerLoanEnabled(context) && AppStore.isLoanRequestText(body)) {
            AppStore.markProcessed(context, eventId);
            processSmartLoanRequest(context, sender, body);
            return;
        }

        // Trusted credit agents / POS numbers use one path only.
        // HOTFIX: يمنع دخول نفس رسالة نقطة البيع في مسار المحافظ أو أي مسار قديم، حتى لا تُنفذ مرتين.
        TrustedCreditAgent creditAgent = AppStore.findTrustedCreditSender(context, sender);
        if (creditAgent != null) {
            String signedBody = AppStore.stripTrustedCreditSignature(creditAgent, body);
            if (signedBody == null) {
                AppStore.markProcessed(context, eventId);
                addLog(context, "أمان نقطة بيع", sender, creditAgent.name, "", 0,
                        "مرفوض بصمت: بصمة غير صحيحة", "تم تجاهل رسالة من رقم موثوق لأنها لا تحتوي بصمة نقطة البيع المسجلة أو تحتوي بصمة غير مطابقة. لم يتم إرسال أي رد.\nالنص:\n" + body, "");
                return;
            }
            if (PaymentParser.isTrustedCreditBalanceRequest(signedBody)) {
                AppStore.markProcessed(context, eventId);
                int requestedAmount = PaymentParser.extractAmountForReview(signedBody);
                String agentPhone = cleanPhone(AppStore.replyPhoneForTrustedCreditAgent(creditAgent, sender));
                String msg = "نقطة بيع طلبت رصيد"
                        + "\nالنقطة: " + (creditAgent.name == null ? "" : creditAgent.name)
                        + "\nالرقم: " + agentPhone
                        + "\nالمتبقي الحالي: " + AppStore.remainingTrustedCredit(creditAgent) + " ريال"
                        + (requestedAmount > 0 ? "\nالمبلغ المطلوب تقريباً: " + requestedAmount + " ريال" : "")
                        + "\nنص الرسالة بعد البصمة:\n" + signedBody;
                String logId = addLog(context, "طلب رصيد نقطة بيع", sender, creditAgent.name, agentPhone, requestedAmount,
                        "معلق: نقطة بيع تحتاج رصيد", msg, "");
                NotifyHelper.notifyPendingAction(context, logId, requestedAmount, "", "نقطة بيع تحتاج رصيد: " + (creditAgent.name == null ? agentPhone : creditAgent.name));
                return;
            }
            ParsedCreditRequest creditRequest = PaymentParser.parseTrustedCreditRequest(context, signedBody);
            if (creditRequest != null) {
                processTrustedCreditRequest(context, eventId, sender, creditAgent, creditRequest);
            } else {
                AppStore.markProcessed(context, eventId);
                if (signedBody.matches(".*\\d.*")) {
                    String logId = addLog(context, "رقم موثوق", sender, creditAgent.name, "", 0,
                            "مراجعة: طلب غير مفهوم", "رسالة من رقم موثوق موقعة ببصمة صحيحة لكن لم يتم فهمها ولم تُحوّل لأي مسار آخر.\nالنص:\n" + signedBody, "");
                    NotifyHelper.notifyPendingAction(context, logId, 0, "", "طلب رقم موثوق غير مفهوم");
                }
            }
            return;
        }

        // Try to parse as a payment message (supports known wallets + custom wallets + fallback)
        ParsedPayment payment = PaymentParser.parse(context, sender, body);

        // أي إيصال/إيداع واضح لم يتم التعرف عليه كعملية قابلة للتنفيذ يدخل إلى المراجعة.
        if (payment == null) {
            if (PaymentParser.looksLikePayment(body)) {
                if (AppStore.isExactPaymentTextProcessed(context, sender, body)) {
                    AppStore.markProcessed(context, eventId);
                    return;
                }
                AppStore.markExactPaymentTextProcessed(context, sender, body);
                AppStore.markProcessed(context, eventId);
                int amount = PaymentParser.extractAmountForReview(body);
                String walletName = PaymentParser.walletDisplayName(sender, body);
                String name = PaymentParser.extractNameForReview(body);
                String pendingLogId = addExplicitDepositReviewLog(context, walletName, sender, name, "", amount,
                        "لم يتم تنفيذ الإيداع لأن الرسالة لم تطابق قوالب المحافظ المعروفة أو لم تحتوي بيانات كافية للتنفيذ. أضف الاسم/الرقم للمحفظة نفسها ثم اضغط تمت المراجعة بعد المعالجة.", body);
                NotifyHelper.notifyExplicitDepositReview(context, pendingLogId, amount, walletName, name, "رسالة إيداع تحتاج مراجعة");
            }
            return;
        }

        // قفل التكرار الحرفي V44: إذا تساوى نص رسالة الدفع كاملًا 100% لا يتم إرسال كرت ولا تدخل للسجل مرة أخرى.
        // أما إذا اختلف رقم العملية أو الرصيد أو أي حرف في النص فتُعامل كعملية جديدة.
        if (AppStore.isExactPaymentTextProcessed(context, sender, body)) {
            AppStore.markProcessed(context, eventId);
            return;
        }
        AppStore.markExactPaymentTextProcessed(context, sender, body);

        // At this point we have a parsed payment
        boolean activated = AppStore.isLicenseUsable(context);
        boolean autoSend = AppStore.isAutoSendEnabled(context);
        boolean trusted = PaymentParser.trustedSender(context, sender, body);

        if (!activated) {
            AppStore.markProcessed(context, eventId);
            String pendingLogId = addExplicitDepositReviewLog(context, payment.provider, sender, payment.customerName, payment.customerPhone, payment.amount,
                    "تم تسجيل عملية إيداع صريحة، لكن التطبيق غير مفعل؛ لن يتم إرسال كرت حتى تتم المراجعة.", body);
            NotifyHelper.notifyExplicitDepositReview(context, pendingLogId, payment.amount, payment.provider, payment.customerName, "التطبيق غير مفعل");
            return;
        }

        if (!trusted) {
            AppStore.markProcessed(context, eventId);
            String pendingLogId = addExplicitDepositReviewLog(context, payment.provider, sender, payment.customerName, payment.customerPhone, payment.amount,
                    "تم تسجيل عملية إيداع صريحة، لكن مصدر الرسالة غير موثوق؛ لن يتم إرسال كرت حتى تتم المراجعة.", body);
            NotifyHelper.notifyExplicitDepositReview(context, pendingLogId, payment.amount, payment.provider, payment.customerName, "مصدر الرسالة غير موثوق");
            return;
        }

        if (!autoSend) {
            AppStore.markProcessed(context, eventId);
            String pendingLogId = addExplicitDepositReviewLog(context, payment.provider, sender, payment.customerName, payment.customerPhone, payment.amount,
                    "تم تسجيل عملية إيداع صريحة، لكن الإرسال التلقائي متوقف؛ لن يتم إرسال كرت حتى تتم المراجعة.", body);
            NotifyHelper.notifyExplicitDepositReview(context, pendingLogId, payment.amount, payment.provider, payment.customerName, "الإرسال التلقائي متوقف");
            return;
        }

        // Now proceed with the normal auto-send flow
        AppStore.markProcessed(context, eventId);

        String receiver = cleanPhone(payment.customerPhone);
        String messageNote = "تمت المعالجة تلقائيًا";

        if (!isValidLocalMobile(receiver)) {
            TrustedContact contact = AppStore.findWalletContact(context, payment.customerName, sender, body);
            if (contact == null) contact = AppStore.findTrustedByName(context, payment.customerName);
            if (contact != null) {
                receiver = cleanPhone(contact.phone);
                messageNote = contact.walletName + ": تم اعتماد الاسم الثلاثي " + contact.tripleName;
            }
        }

        if (!isValidLocalMobile(receiver)) {
            String name = payment.customerName == null ? "" : payment.customerName.trim();
            String note = name.isEmpty()
                    ? "تم التعرف على عملية إيداع صحيحة لكن لا يوجد رقم زبون صحيح مكون من 9 أرقام ويبدأ بـ 7. راجع الرسالة ثم احذفها أو أضف الاسم إلى المحافظ الموثوقة إذا كنت تعرف رقم العميل."
                    : "تم التعرف على عملية إيداع صحيحة باسم: " + name + "، لكن لا يوجد رقم زبون صحيح مكون من 9 أرقام ويبدأ بـ 7. من السجل يمكنك الضغط على زر إضافة الاسم للمحافظ الموثوقة أو حذف الإشعار.";
            String pendingLogId = addLog(context, payment.provider, sender, name, "", payment.amount, "إيداع صريح غير منفذ: يحتاج إضافة اسم", note + "\nنص الرسالة الأصلي:\n" + body, "");
            NotifyHelper.notifyExplicitDepositReview(context, pendingLogId, payment.amount, payment.provider, name, "لا يوجد رقم استلام صحيح");
            return;
        }

        int saleAmount = payment.amount;
        String settlementCustomerPart = "";
        String settlementInternalNote = "";

        // STAGE 12.6 REAL FIX:
        // أي إيداع من زبون موجود في الدفتر المحاسبي يُعامل كسداد/رصيد فقط.
        // لا يتم صرف كرت بسبب الإيداع نهائياً؛ صرف الكرت يكون فقط عندما يرسل الزبون رقم الفئة بنفسه مثل 100.
        if (AppStore.isSmartLedgerEnabled(context) && AppStore.isLedgerAutoSettleEnabled(context)) {
            LedgerCustomer debtor = AppStore.findLedgerCustomerByPhone(context, receiver);
            if (debtor != null && debtor.active) {
                int debtBefore = debtor.debt;
                int creditBefore = debtor.creditBalance;
                String method = friendlyPaymentMethod(payment.provider, sender, body);
                LedgerCustomer updated = AppStore.applyLedgerPaymentByPhone(context, receiver, payment.amount, method,
                        "سداد من " + method + " بمبلغ " + payment.amount + " ر.ي. المتبقي بعد السداد: " + Math.max(0, debtBefore - Math.min(debtBefore, payment.amount)) + " ر.ي", method);
                int paidToDebt = Math.min(debtBefore, payment.amount);
                int addedCredit = Math.max(0, payment.amount - paidToDebt);
                String displayName = updated.name == null || updated.name.trim().isEmpty() ? "عميلنا" : updated.name.trim();
                String msg;
                if (updated.debt <= 0 && addedCredit > 0) {
                    msg = "مرحبًا " + displayName + "👋\n"
                            + "✅ وصل " + payment.amount + "ر.ي\n"
                            + "💰تم السداد كاملًا\n"
                            + "رصيدك:" + updated.creditBalance + "ر.ي\n"
                            + AppStore.SYSTEM_SIGNATURE;
                } else if (updated.debt <= 0) {
                    msg = "مرحبًا " + displayName + "👋\n"
                            + "✅ وصل " + payment.amount + "ر.ي\n"
                            + "💰تم السداد كاملًا\n"
                            + "عليك:0ر.ي\n"
                            + AppStore.SYSTEM_SIGNATURE;
                } else {
                    msg = "مرحبًا " + displayName + "👋\n"
                            + "✅ وصل " + payment.amount + "ر.ي\n"
                            + "💰المتبقي:" + updated.debt + "ر.ي\n"
                            + AppStore.SYSTEM_SIGNATURE;
                }
                String logId = addLog(context, payment.provider, sender, payment.customerName, receiver, payment.amount,
                        "جاري إرسال SMS", messageNote + "\nتم ترحيل الإيداع كسداد في الدفتر. الدين السابق: " + debtBefore
                                + " | رصيد سابق: " + creditBefore + " | رصيد حالي: " + updated.creditBalance
                                + " | المتبقي عليه: " + updated.debt, "");
                sendSmsWithTracking(context, receiver, msg, logId, payment.amount, "", false,
                        "تم إرسال رسالة السداد للزبون",
                        "تم تسجيل السداد لكن فشل إرسال SMS؛ راجع كشف الحساب");
                AppStore.performAutoBackupIfDue(context, "سداد دفتر محاسبي");
                return;
            }
        }

        CardItem card = AppStore.takeAvailableCard(context, saleAmount, receiver);
        if (card == null) {
            String pendingLogId = addLog(context, payment.provider, sender, payment.customerName, receiver, saleAmount,
                    "إيداع صريح غير منفذ: نفدت الكمية", "تم تسجيل عملية إيداع صريحة لكن لا توجد كروت متاحة للفئة المطلوبة بعد أي تسوية دين. لم يتم سحب كرت ولم يتم إرسال رسالة كرت.\n" + settlementInternalNote + "\nنص الرسالة الأصلي:\n" + body, "");
            NotifyHelper.notifyExplicitDepositReview(context, pendingLogId, saleAmount, payment.provider, payment.customerName, "نفدت فئة " + saleAmount);
            AppStore.performAutoBackupIfDue(context, "عملية تلقائية - نفاد فئة");
            return;
        }

        String cardMessage = settlementCustomerPart + AppStore.buildSuccessMessage(context, saleAmount, card.code);
        RewardResult reward = AppStore.applyRewardsForPaidSale(context, receiver, payment.customerName, saleAmount, card.code);
        if (reward != null && reward.customerMessagePart != null && !reward.customerMessagePart.isEmpty()) {
            cardMessage += reward.customerMessagePart;
        }
        String rewardNote = (reward == null || reward.internalNote == null || reward.internalNote.isEmpty()) ? "" : "\n" + reward.internalNote;
        String logId = addLog(context, payment.provider, sender, payment.customerName, receiver, saleAmount,
                "جاري إرسال SMS", messageNote + (settlementInternalNote.isEmpty() ? "" : "\n" + settlementInternalNote) + rewardNote, card.code);

        sendSmsWithTracking(context, receiver, cardMessage, logId, saleAmount, card.code, false,
                settlementCustomerPart.isEmpty() ? "تم إرسال الكرت بنجاح" : "تم السداد الآلي وإرسال الكرت بالباقي",
                "فشل إرسال الكرت؛ استخدم إعادة إرسال SMS أو واتساب");

        AppStore.performAutoBackupIfDue(context, settlementCustomerPart.isEmpty() ? "عملية تلقائية" : "سداد آلي + صرف كرت بالباقي");
    }


    private static String addExplicitDepositReviewLog(Context context, String provider, String sender, String customerName, String customerPhone, int amount, String note, String originalBody) {
        String name = customerName == null ? "" : customerName.trim();
        String phone = customerPhone == null ? "" : cleanPhone(customerPhone);
        String wallet = provider == null || provider.trim().isEmpty() ? PaymentParser.walletDisplayName(sender, originalBody) : provider.trim();
        StringBuilder msg = new StringBuilder();
        msg.append("إيداع صريح غير منفذ يحتاج مراجعة من الإدارة.");
        if (note != null && !note.trim().isEmpty()) msg.append("\n").append(note.trim());
        if (name.isEmpty()) {
            String extracted = PaymentParser.extractNameForReview(originalBody);
            if (extracted != null && !extracted.trim().isEmpty()) name = extracted.trim();
        }
        if (originalBody != null && !originalBody.trim().isEmpty() && msg.indexOf("نص الرسالة الأصلي") < 0) {
            msg.append("\nنص الرسالة الأصلي:\n").append(originalBody.trim());
        }
        return addLog(context, wallet, sender, name, phone, amount, "إيداع صريح غير منفذ: يحتاج مراجعة", msg.toString(), "");
    }


    private static void processSmartLoanRequest(Context context, String sender, String body) {
        String customerPhone = cleanPhone(sender);
        int amount = AppStore.extractLoanAmount(body);
        if (!isValidLocalMobile(customerPhone)) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, "", "", amount, "سلفة مرفوضة", "طلب سلفة من رقم غير صالح. النص:\n" + body, "");
            NotifyHelper.notifyPendingAction(context, logId, amount, "", "طلب سلفة من رقم غير صالح");
            return;
        }
        LedgerCustomer customer = AppStore.findLedgerCustomerByPhone(context, customerPhone);
        if (customer == null) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, "", customerPhone, amount, "سلفة مرفوضة", "الرقم غير موجود في الدفتر المحاسبي. النص:\n" + body, "");
            String msg = "تعذر تنفيذ الطلب.\nالرقم غير مسجل في الدفتر.\n" + AppStore.SYSTEM_SIGNATURE;
            sendSmsWithTracking(context, customerPhone, msg, logId, amount, "", false,
                    "تم إرسال سبب رفض السلفة", "فشل إرسال سبب رفض السلفة");
            return;
        }
        if (!customer.active || customer.isStopped()) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, amount, "سلفة مرفوضة", "حساب العميل موقوف. النص:\n" + body, "");
            String msg = "تعذر تنفيذ الطلب.\nحسابك موقوف مؤقتًا.\n" + AppStore.SYSTEM_SIGNATURE;
            sendSmsWithTracking(context, customerPhone, msg, logId, amount, "", false,
                    "تم إرسال سبب رفض السلفة", "فشل إرسال سبب رفض السلفة");
            return;
        }
        if (customer.isSettleOnly()) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, amount, "سلفة مرفوضة", "العميل على وضع سداد فقط / إيقاف السلف. النص:\n" + body, "");
            String msg = "السلفة موقوفة لحسابك.\nيمكن استقبال السداد فقط.\nالمتبقي عليك:" + customer.debt + "ر.ي\n" + AppStore.SYSTEM_SIGNATURE;
            sendSmsWithTracking(context, customerPhone, msg, logId, amount, "", false,
                    "تم إرسال تنبيه إيقاف السلف", "فشل إرسال تنبيه إيقاف السلف");
            return;
        }
        if (!customer.canAutoLoan()) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, amount, "سلفة مرفوضة", "العميل ليس في وضع آلي كامل. الوضع الحالي: " + AppStore.ledgerModeLabel(customer.effectiveMode()), "");
            String msg = "تعذر تنفيذ طلب السلفة.\nوضع حسابك لا يسمح بالسلف.\n" + AppStore.SYSTEM_SIGNATURE;
            sendSmsWithTracking(context, customerPhone, msg, logId, amount, "", false,
                    "تم إرسال سبب رفض السلفة", "فشل إرسال سبب رفض السلفة");
            return;
        }
        if (amount <= 0) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, 0, "سلفة مرفوضة", "لم يتم تحديد فئة صحيحة في رسالة السلفة. النص:\n" + body, "");
            String msg = "صيغة الطلب غير صحيحة.\nاكتب رقم الفئة فقط مثل: 100\n" + AppStore.SYSTEM_SIGNATURE;
            sendSmsWithTracking(context, customerPhone, msg, logId, 0, "", false,
                    "تم إرسال تنبيه صيغة السلفة", "فشل إرسال تنبيه صيغة السلفة");
            return;
        }

        int useCredit = Math.min(Math.max(0, customer.creditBalance), amount);
        int loanPart = Math.max(0, amount - useCredit);
        int debtAfter = Math.max(0, customer.debt) + loanPart;
        if (debtAfter > customer.loanLimit) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, amount, "سلفة مرفوضة: تجاوز السقف",
                    "الدين الحالي: " + customer.debt + " | رصيد العميل: " + customer.creditBalance + " | سيستخدم من الرصيد: " + useCredit + " | سيسجل كسلفة: " + loanPart + " | السقف: " + customer.loanLimit, "");
            String msg = "تعذر تنفيذ الطلب.\n"
                    + "المتاح من السقف:" + customer.remainingLoan() + "ر.ي\n"
                    + "رصيدك:" + customer.creditBalance + "ر.ي\n"
                    + "المطلوب:" + amount + "ر.ي\n"
                    + "العملية تتجاوز السقف.\n"
                    + AppStore.SYSTEM_SIGNATURE;
            sendSmsWithTracking(context, customerPhone, msg, logId, amount, "", false,
                    "تم إرسال تنبيه تجاوز حد السلفة", "فشل إرسال تنبيه تجاوز حد السلفة");
            return;
        }
        CardItem card = AppStore.takeAvailableCard(context, amount, customerPhone);
        if (card == null) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, amount, "سلفة معلقة: نفدت الكمية",
                    "طلب سلفة صحيح لكن لا توجد كروت متاحة لهذه الفئة. لم يتم زيادة الدين ولم يتم استخدام الرصيد.", "");
            NotifyHelper.notifyPendingAction(context, logId, amount, "", "نفدت فئة طلب السلفة");
            String msg = AppStore.buildNoStockMessage(context, amount);
            sendSmsWithTracking(context, customerPhone, msg, logId, amount, "", true,
                    "تم إرسال تنبيه نفاد فئة السلفة", "فشل إرسال تنبيه نفاد فئة السلفة");
            return;
        }
        LedgerCustomer updated = AppStore.applyLedgerLoanWithCredit(context, customerPhone, amount, card.code);
        if (updated == null) {
            String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, amount, "سلفة مرفوضة: تجاوز السقف",
                    "فشل تحديث الدفتر بعد حجز الكرت؛ راجع العملية يدويًا. الكرت المحجوز: " + card.code, card.code);
            NotifyHelper.notifyPendingAction(context, logId, amount, card.code, "مراجعة عاجلة: كرت حُجز ولم تسجل السلفة");
            return;
        }
        String displayName = customer.name == null || customer.name.trim().isEmpty() ? "عميلنا" : customer.name.trim();
        int actualUseCredit = Math.max(0, amount - Math.max(0, updated.debt - customer.debt));
        String msg = "مرحبًا " + displayName + "👋\n"
                + "🎫 كرت " + amount + "ر.ي\n"
                + card.code + "\n"
                + "💰عليك:" + updated.debt + "ر.ي\n"
                + (actualUseCredit > 0 ? "رصيد مستخدم:" + actualUseCredit + "ر.ي\n" : "")
                + AppStore.SYSTEM_SIGNATURE;
        String logId = addLog(context, "الدفتر المحاسبي", sender, customer.name, customerPhone, amount, "جاري إرسال SMS",
                "تم صرف كرت كسلفة. استخدم من الرصيد: " + actualUseCredit + " | المتبقي على الزبون: " + updated.debt + " | رصيد العميل الحالي: " + updated.creditBalance, card.code);
        sendSmsWithTracking(context, customerPhone, msg, logId, amount, card.code, false,
                "تم إرسال كرت السلفة وتحديث الدفتر",
                "تم تحديث الدفتر لكن فشل إرسال SMS؛ استخدم إعادة الإرسال أو واتساب");
        AppStore.performAutoBackupIfDue(context, "سلفة ذكية من الدفتر");
    }

    private static void processTrustedCreditRequest(Context context, String eventId, String sender, TrustedCreditAgent agent, ParsedCreditRequest req) {
        AppStore.markProcessed(context, eventId);
        String customerPhone = cleanPhone(req.customerPhone);
        String agentPhone = cleanPhone(AppStore.replyPhoneForTrustedCreditAgent(agent, sender));
        if (agentPhone.isEmpty()) agentPhone = cleanPhone(agent.senderPhone);
        String agentName = agent.name == null || agent.name.trim().isEmpty() ? agentPhone : agent.name.trim();
        String provider = "رقم موثوق";
        int totalAmount = req.totalAmount();

        if (req == null || !req.isValid()) {
            String reason = req == null ? "تعذر تحليل الطلب." : req.rejectReason;
            String logId = addLog(context, provider, sender, agentName, customerPhone, totalAmount,
                    "مراجعة: طلب ملتبس", "لم يتم تنفيذ طلب الرقم الموثوق لأن الرسالة غير واضحة أو فيها التباس.\nالسبب: " + reason, "");
            NotifyHelper.notifyPendingAction(context, logId, totalAmount, "", "طلب رقم موثوق يحتاج مراجعة");
            if (!agentPhone.isEmpty()) {
                String msg = (reason != null && reason.contains("غير معتمدة"))
                        ? AppStore.buildPosInvalidCategoryMessage(context, agent, req, reason)
                        : AppStore.buildPosAmbiguousMessage(context, agent, req, reason);
                sendSmsWithTracking(context, agentPhone, msg, logId, totalAmount, "", true,
                        "تم إرسال تنبيه الطلب الملتبس للرقم الموثوق",
                        "تعذر إرسال تنبيه الطلب الملتبس للرقم الموثوق");
            }
            return;
        }

        if (!AppStore.isLicenseUsable(context)) {
            addLog(context, provider, sender, agentName, customerPhone, totalAmount, "وارد", "طلب من رقم موثوق، لكن التطبيق غير مفعل؛ لن يتم إرسال كرت", "");
            return;
        }
        if (!AppStore.isAutoSendEnabled(context)) {
            addLog(context, provider, sender, agentName, customerPhone, totalAmount, "وارد", "طلب من رقم موثوق، لكن الإرسال التلقائي متوقف؛ لن يتم إرسال كرت", "");
            return;
        }
        if (!isValidLocalMobile(customerPhone)) {
            String logId = addLog(context, provider, sender, agentName, "", totalAmount, "مرفوض", "طلب رقم موثوق بدون رقم زبون صحيح. الرقم يجب أن يكون 9 أرقام ويبدأ بـ 7.", "");
            if (!agentPhone.isEmpty()) {
                String msg = AppStore.buildPosInvalidPhoneMessage(context, agent, req, "رقم العميل غير صحيح أو غير مكتمل");
                sendSmsWithTracking(context, agentPhone, msg, logId, totalAmount, "", true,
                        "تم إرسال تنبيه الرقم الخاطئ للرقم الموثوق",
                        "تعذر إرسال تنبيه الرقم الخاطئ للرقم الموثوق");
            }
            return;
        }

        String requestLockKey = agent.id + "|" + req.fingerprint;
        if (!AppStore.reserveTrustedRequest(context, requestLockKey, 5L * 60L * 1000L)) {
            String msg = AppStore.buildPosDuplicateMessage(context, agent, req);
            String logId = addLog(context, provider, sender, agentName, customerPhone, totalAmount, "مكرر خلال 5 دقائق",
                    "تم رفض الطلب لأنه يطابق طلباً سابقاً لنفس رقم العميل ونفس الفئات خلال أقل من 5 دقائق.\nالفئات: " + req.amountsText(), "");
            if (!agentPhone.isEmpty()) {
                sendSmsWithTracking(context, agentPhone, msg, logId, totalAmount, "", true,
                        "تم إرسال تنبيه التكرار للرقم الموثوق",
                        "تعذر إرسال تنبيه التكرار للرقم الموثوق");
            }
            return;
        }

        if (!AppStore.canUseTrustedCredit(agent, totalAmount)) {
            String msg = AppStore.buildPosLimitMessage(context, agent, req);
            String logId = addLog(context, provider, sender, agentName, customerPhone, totalAmount, "تجاوز السقف", "لم يتم إرسال كرت. " + msg, "");
            if (!agentPhone.isEmpty()) {
                sendSmsWithTracking(context, agentPhone, msg, logId, totalAmount, "", true,
                        "تم إرسال تنبيه السقف للرقم الموثوق",
                        "تعذر إرسال تنبيه السقف للرقم الموثوق");
            }
            return;
        }

        ArrayList<CardItem> cards = AppStore.takeAvailableCardsAllOrNone(context, req.amounts, customerPhone);
        if (cards == null || cards.size() != req.countCards()) {
            String pendingLogId = addLog(context, provider, sender, agentName, customerPhone, totalAmount, "معلق: نفدت الكمية",
                    "طلب رقم موثوق صحيح لكن لا توجد كروت كافية لتنفيذ الطلب كاملاً. لم يتم خصم السقف وسيبقى الطلب للمراجعة.\nالفئات المطلوبة: " + req.amountsText(), "");
            NotifyHelper.notifySendFailed(context, totalAmount, "", "نفدت إحدى الفئات المطلوبة");
            NotifyHelper.notifyPendingAction(context, pendingLogId, totalAmount, "", "طلب رقم موثوق معلّق بسبب نقص الكروت");
            if (!agentPhone.isEmpty()) {
                String msg = AppStore.buildPosNoStockMessage(context, agent, req);
                sendSmsWithTracking(context, agentPhone, msg, pendingLogId, totalAmount, "", true,
                        "تم إرسال تنبيه نقص الكروت للرقم الموثوق",
                        "تعذر إرسال تنبيه نقص الكروت للرقم الموثوق");
            }
            return;
        }

        int usedAfter = agent.usedAmount + totalAmount;
        int remainingAfter = Math.max(0, agent.creditLimit - usedAfter);
        StringBuilder cardCodes = new StringBuilder();
        boolean effectiveRewards = AppStore.isRewardsEnabled(context) && agent.rewardsEnabled;
        StringBuilder rewardNotes = new StringBuilder();
        StringBuilder rewardCustomerParts = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            CardItem card = cards.get(i);
            if (cardCodes.length() > 0) cardCodes.append(" | ");
            cardCodes.append(card.amount).append(":").append(card.code);
            if (effectiveRewards) {
                RewardResult reward = AppStore.applyRewardsForPaidSale(context, customerPhone, agentName, card.amount, card.code);
                if (reward != null && reward.customerMessagePart != null && !reward.customerMessagePart.trim().isEmpty()) {
                    if (rewardCustomerParts.length() > 0) rewardCustomerParts.append("\n");
                    rewardCustomerParts.append(reward.customerMessagePart.trim());
                }
                if (reward != null && reward.internalNote != null && !reward.internalNote.isEmpty()) rewardNotes.append("\n").append(card.amount).append(" -> ").append(reward.internalNote);
            }
        }

        String note = "تم تنفيذ طلب رقم موثوق: " + agentName
                + "\nرقم الطلب المرسل: " + cleanPhone(sender)
                + "\nمصدر الرقم: " + (cleanPhone(sender).equals(cleanPhone(agent.senderPhone)) ? "الرقم الأساسي" : "الرقم الإضافي")
                + "\nالفئات: " + req.amountsText()
                + "\nعدد الكروت: " + req.countCards()
                + "\nإجمالي الطلب: " + totalAmount
                + "\nالسقف: " + agent.creditLimit
                + "\nسيخصم من السقف بعد نجاح إرسال SMS"
                + "\nالمستخدم المتوقع بعد النجاح: " + usedAfter;
        if (rewardNotes.length() > 0) note += rewardNotes.toString();
        if (!effectiveRewards) note += "\nالمكافأة معطلة لهذا الرقم الموثوق؛ تم تنفيذ البيع بدون احتساب نقاط جديدة.";
        String logId = addLog(context, provider, sender, agentName, customerPhone, totalAmount, "جاري إرسال SMS", note, cardCodes.toString());

        boolean sameReceiverAsAgent = !agentPhone.isEmpty() && agentPhone.equals(customerPhone);
        String customerText = sameReceiverAsAgent
                ? AppStore.buildPosSelfSaleMessage(context, cards, rewardCustomerParts.toString(), remainingAfter)
                : AppStore.buildGroupedCardsMessage(context, cards, rewardCustomerParts.toString());
        String agentSuccess = sameReceiverAsAgent ? "" : AppStore.buildPosSuccessMessage(context, agent, req, remainingAfter);
        sendSmsWithTracking(context, customerPhone, customerText, logId, totalAmount, cardCodes.toString(), false,
                sameReceiverAsAgent ? "تم إرسال رسالة واحدة لنقطة البيع نفسها وتم خصم العملية من السقف" : "تم إرسال الكرت/الكروت للزبون وتم خصم العملية من السقف",
                "فشل إرسال SMS لزبون الرقم الموثوق؛ لم يتم خصم السقف. استخدم إعادة إرسال SMS أو واتساب",
                agent.id, totalAmount, sameReceiverAsAgent ? "" : agentPhone, agentSuccess);
        AppStore.performAutoBackupIfDue(context, "طلب رقم موثوق ذكي");
    }


    static void directSale(Context context, int amount, String phone, String customerName) {
        if (!AppStore.isLicenseUsable(context)) return;
        String receiver = cleanPhone(phone);
        String name = customerName == null ? "" : customerName.trim();
        if (!isValidLocalMobile(receiver)) {
            String logId = addLog(context, "بيع مباشر", "manual", name, "", amount, "لا يوجد رقم", "لم يتم البيع لأن رقم الزبون غير صحيح. الرقم يجب أن يكون 9 أرقام ويبدأ بـ 7.", "");
            NotifyHelper.notifySendFailed(context, amount, "", "لا يوجد رقم صحيح");
            return;
        }

        CardItem card = AppStore.takeAvailableCard(context, amount, receiver);
        if (card == null) {
            addLog(context, "بيع مباشر", "manual", name, receiver, amount,
                    "فشل البيع: نفدت الكمية",
                    "بيع مباشر: لا توجد كروت متاحة لهذه الفئة. لم يتم إرسال أي رسالة للزبون لأن البيع من داخل التطبيق.", "");
            NotifyHelper.notifySendFailed(context, amount, "", "نفدت الكمية في البيع المباشر");
            AppStore.performAutoBackupIfDue(context, "بيع مباشر SMS - نفاد فئة بدون إرسال");
            return;
        }

        String reply = AppStore.buildDirectSaleMessage(context, amount, card.code);
        String logId = addLog(context, "بيع مباشر", "manual", name, receiver, amount,
                "جاري إرسال SMS", "بيع مباشر: تم حجز الكرت وجاري إرسال الرسالة للزبون\nتنبيه: البيع المباشر لا يضيف نقاطًا للعميل.", card.code);
        sendSmsWithTracking(context, receiver, reply, logId, amount, card.code, false,
                "تم إرسال رسالة الكرت للزبون من البيع المباشر",
                "تم حجز الكرت لكن فشل إرسال SMS من البيع المباشر؛ استخدم إعادة إرسال SMS أو واتساب");
        AppStore.performAutoBackupIfDue(context, "بيع مباشر SMS");
    }

    static void resendFromLog(Context context, OperationLog log) {
        if (log == null) return;
        String receiver = cleanPhone(log.customerPhone);
        if (receiver == null || receiver.isEmpty()) {
            AppStore.updateLogStatus(context, log.id, "لا يوجد رقم", "لا يمكن إعادة الإرسال لأن رقم الزبون غير موجود.");
            NotifyHelper.notifySendFailed(context, log.amount, log.cardCode, "لا يوجد رقم");
            return;
        }
        boolean noStock = log.cardCode == null || log.cardCode.trim().isEmpty();
        boolean directSale = "manual".equalsIgnoreCase(log.sender) || (log.provider != null && log.provider.contains("بيع مباشر"));
        if (noStock && directSale) {
            AppStore.updateLogStatus(context, log.id, "لم يتم الإرسال", "بيع مباشر بدون كمية: لا توجد رسالة لإعادة إرسالها لأن البيع لم يكتمل.");
            return;
        }
        String msg = noStock
                ? AppStore.buildNoStockMessage(context, log.amount)
                : (directSale ? AppStore.buildDirectSaleMessage(context, log.amount, log.cardCode) : AppStore.buildSuccessMessage(context, log.amount, log.cardCode));
        AppStore.updateLogStatus(context, log.id, "جاري إعادة إرسال SMS", "تم الضغط على إعادة الإرسال؛ جاري إرسال الرسالة مرة أخرى.");
        sendSmsWithTracking(context, receiver, msg, log.id, log.amount, log.cardCode, noStock,
                noStock ? "تم إرسال تنبيه النفاد للزبون بعد إعادة المحاولة" : "تم إرسال رسالة الكرت للزبون بعد إعادة المحاولة",
                noStock ? "فشل إعادة إرسال تنبيه النفاد؛ جرّب واتساب أو افحص الرصيد والشريحة" : "فشل إعادة إرسال SMS؛ جرّب واتساب أو افحص الرصيد والشريحة");
    }


    private static String friendlyPaymentMethod(String provider, String sender, String body) {
        String src = ((provider == null ? "" : provider) + " " + (sender == null ? "" : sender) + " " + (body == null ? "" : body)).toLowerCase();
        if (src.contains("one") || src.contains("ون") || src.contains("1cash")) return "ون كاش";
        if (src.contains("jawali") || src.contains("جوالي")) return "جوالي";
        if (src.contains("jaib") || src.contains("جيب")) return "جيب";
        if (src.contains("kuraimi") || src.contains("كريمي")) return "كريمي";
        if (src.contains("floosk") || src.contains("فلوسك")) return "فلوسك";
        if (src.contains("محل") || src.contains("نقد")) return "عن طريق المحل";
        String p = provider == null ? "" : provider.trim();
        return p.isEmpty() ? "إيداع" : p;
    }

    static String cleanPhone(String phone) {
        return PaymentParser.normalizeLocalPhone(phone);
    }

    static boolean isValidLocalMobile(String phone) {
        return PaymentParser.hasValidLocalMobile(phone);
    }

    static boolean sendSmsWithTracking(Context context, String phone, String text, String logId, int amount, String cardCode, boolean noStock, String successMsg, String failMsg) {
        return sendSmsWithTracking(context, phone, text, logId, amount, cardCode, noStock, successMsg, failMsg, "", 0, "", "");
    }

    static boolean sendSmsWithTracking(Context context, String phone, String text, String logId, int amount, String cardCode, boolean noStock, String successMsg, String failMsg, String trustedCreditAgentId, int trustedCreditAmount) {
        return sendSmsWithTracking(context, phone, text, logId, amount, cardCode, noStock, successMsg, failMsg, trustedCreditAgentId, trustedCreditAmount, "", "");
    }

    static boolean sendSmsWithTracking(Context context, String phone, String text, String logId, int amount, String cardCode, boolean noStock, String successMsg, String failMsg, String trustedCreditAgentId, int trustedCreditAmount, String trustedNotifyPhone, String trustedNotifyText) {
        try {
            String clean = cleanPhone(phone);
            if (!isValidLocalMobile(clean)) return false;
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(text == null ? "" : text);
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            for (int i = 0; i < parts.size(); i++) {
                Intent intent = new Intent(context, SmsStatusReceiver.class);
                intent.setAction(ACTION_SMS_SENT);
                intent.putExtra("logId", logId);
                intent.putExtra("phone", clean);
                intent.putExtra("amount", amount);
                intent.putExtra("cardCode", cardCode == null ? "" : cardCode);
                intent.putExtra("noStock", noStock);
                intent.putExtra("part", i + 1);
                intent.putExtra("total", parts.size());
                intent.putExtra("successMsg", successMsg == null ? "تم إرسال SMS" : successMsg);
                intent.putExtra("failMsg", failMsg == null ? "فشل إرسال SMS" : failMsg);
                intent.putExtra("trustedCreditAgentId", trustedCreditAgentId == null ? "" : trustedCreditAgentId);
                intent.putExtra("trustedCreditAmount", trustedCreditAmount);
                intent.putExtra("trustedNotifyPhone", trustedNotifyPhone == null ? "" : cleanPhone(trustedNotifyPhone));
                intent.putExtra("trustedNotifyText", trustedNotifyText == null ? "" : trustedNotifyText);
                int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                if (android.os.Build.VERSION.SDK_INT >= 23) flags |= PendingIntent.FLAG_IMMUTABLE;
                sentIntents.add(PendingIntent.getBroadcast(context, (logId + i).hashCode(), intent, flags));
            }
            sms.sendMultipartTextMessage(clean, null, parts, sentIntents, null);
            return true;
        } catch (Exception e) {
            AppStore.updateLogStatus(context, logId, "فشل إرسال SMS", failMsg + "\nالسبب: " + e.getMessage());
            if (noStock) NotifyHelper.notifyNoStockFailed(context, amount, phone);
            else NotifyHelper.notifySendFailed(context, amount, cardCode, phone);
            return false;
        }
    }

    private static String addLog(Context context, String provider, String sender, String name, String phone, int amount, String status, String message, String cardCode) {
        String id = UUID.randomUUID().toString();
        AppStore.addLog(context, new OperationLog(
                id,
                provider == null ? "" : provider,
                sender == null ? "" : sender,
                name == null ? "" : name,
                phone == null ? "" : cleanPhone(phone),
                amount,
                status,
                message == null ? "" : message,
                cardCode == null ? "" : cardCode,
                AppStore.now()
        ));
        return id;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(value.hashCode());
        }
    }
}
