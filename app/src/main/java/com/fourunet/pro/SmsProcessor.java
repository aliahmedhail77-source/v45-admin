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
        if (PaymentParser.isNonPaymentNoise(body)) return false;
        if (AppStore.findTrustedCreditSender(context, sender) != null && PaymentParser.parseTrustedCreditRequest(body) != null) return true;
        if (PaymentParser.parse(context, sender, body) != null) return true;
        return PaymentParser.looksLikePayment(body);
    }

    static void processIncomingSms(Context context, String sender, String body) {
        processIncomingSms(context, sender, body, System.currentTimeMillis());
    }

    static void processIncomingSms(Context context, String sender, String body, long receivedAt) {
        if (context == null || body == null) return;

        // Deduplicate the same SMS PDU only, not repeated identical requests.
        // Old versions used sender+body only, so repeated messages like
        // "تم إضافة 100 من-776901570" were ignored after the first one.
        // Including the SMS timestamp lets each real incoming SMS be processed,
        // while repeated delivery of the same SMS broadcast stays protected.
        if (receivedAt <= 0L) receivedAt = System.currentTimeMillis();
        String eventId = sha256((sender == null ? "" : sender) + "|" + receivedAt + "|" + body);
        if (AppStore.isProcessed(context, eventId)) return;

        // Trusted credit agents: authorized POS/staff can send: تم اضافة 100 من-733938509
        TrustedCreditAgent creditAgent = AppStore.findTrustedCreditSender(context, sender);
        ParsedCreditRequest creditRequest = creditAgent == null ? null : PaymentParser.parseTrustedCreditRequest(body);
        if (creditAgent != null && creditRequest != null) {
            if (AppStore.isExactPaymentTextProcessed(context, sender, body)) {
                AppStore.markProcessed(context, eventId);
                return;
            }
            AppStore.markExactPaymentTextProcessed(context, sender, body);
            processTrustedCreditRequest(context, eventId, sender, creditAgent, creditRequest);
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

        CardItem card = AppStore.takeAvailableCard(context, payment.amount, receiver);
        if (card == null) {
            String pendingLogId = addLog(context, payment.provider, sender, payment.customerName, receiver, payment.amount,
                    "إيداع صريح غير منفذ: نفدت الكمية", "تم تسجيل عملية إيداع صريحة لكن لا توجد كروت متاحة لهذه الفئة. لم يتم سحب كرت ولم يتم إرسال رسالة كرت.\nنص الرسالة الأصلي:\n" + body, "");
            NotifyHelper.notifyExplicitDepositReview(context, pendingLogId, payment.amount, payment.provider, payment.customerName, "نفدت فئة " + payment.amount);
            AppStore.performAutoBackupIfDue(context, "عملية تلقائية - نفاد فئة");
            return;
        }

        String cardMessage = AppStore.buildSuccessMessage(context, payment.amount, card.code);
        RewardResult reward = AppStore.applyRewardsForPaidSale(context, receiver, payment.customerName, payment.amount, card.code);
        if (reward != null && reward.customerMessagePart != null && !reward.customerMessagePart.isEmpty()) {
            cardMessage += reward.customerMessagePart;
        }
        String rewardNote = (reward == null || reward.internalNote == null || reward.internalNote.isEmpty()) ? "" : "\n" + reward.internalNote;
        String logId = addLog(context, payment.provider, sender, payment.customerName, receiver, payment.amount,
                "جاري إرسال SMS", messageNote + rewardNote, card.code);

        sendSmsWithTracking(context, receiver, cardMessage, logId, payment.amount, card.code, false,
                "تم إرسال الكرت بنجاح",
                "فشل إرسال الكرت؛ استخدم إعادة إرسال SMS أو واتساب");

        AppStore.performAutoBackupIfDue(context, "عملية تلقائية");
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

    private static void processTrustedCreditRequest(Context context, String eventId, String sender, TrustedCreditAgent agent, ParsedCreditRequest req) {
        AppStore.markProcessed(context, eventId);
        String customerPhone = cleanPhone(req.customerPhone);
        String agentPhone = cleanPhone(agent.senderPhone);
        String agentName = agent.name == null || agent.name.trim().isEmpty() ? agentPhone : agent.name.trim();
        String provider = "رقم موثوق";

        if (!AppStore.isLicenseUsable(context)) {
            addLog(context, provider, sender, agentName, customerPhone, req.amount, "وارد", "طلب من رقم موثوق، لكن التطبيق غير مفعل؛ لن يتم إرسال كرت", "");
            return;
        }
        if (!AppStore.isAutoSendEnabled(context)) {
            addLog(context, provider, sender, agentName, customerPhone, req.amount, "وارد", "طلب من رقم موثوق، لكن الإرسال التلقائي متوقف؛ لن يتم إرسال كرت", "");
            return;
        }
        if (!isValidLocalMobile(customerPhone)) {
            addLog(context, provider, sender, agentName, "", req.amount, "مرفوض", "طلب رقم موثوق بدون رقم زبون صحيح. الرقم يجب أن يكون 9 أرقام ويبدأ بـ 7.", "");
            return;
        }
        if (!AppStore.canUseTrustedCredit(agent, req.amount)) {
            String msg = AppStore.buildTrustedCreditLimitMessage(agent);
            String logId = addLog(context, provider, sender, agentName, customerPhone, req.amount, "تجاوز السقف", "لم يتم إرسال كرت. " + msg, "");
            if (!agentPhone.isEmpty()) {
                sendSmsWithTracking(context, agentPhone, msg, logId, req.amount, "", true,
                        "تم إرسال تنبيه السقف للرقم الموثوق",
                        "تعذر إرسال تنبيه السقف للرقم الموثوق");
            }
            return;
        }

        CardItem card = AppStore.takeAvailableCard(context, req.amount, customerPhone);
        if (card == null) {
            String pendingLogId = addLog(context, provider, sender, agentName, customerPhone, req.amount, "معلق: نفدت الكمية",
                    "طلب رقم موثوق صحيح لكن لا توجد كروت متاحة لهذه الفئة. لم يتم خصم السقف وسيبقى الطلب للمراجعة.", "");
            NotifyHelper.notifySendFailed(context, req.amount, "", "نفدت فئة " + req.amount);
            NotifyHelper.notifyPendingAction(context, pendingLogId, req.amount, "", "طلب رقم موثوق معلّق بسبب نفاد فئة " + req.amount);
            return;
        }

        int usedAfter = agent.usedAmount + req.amount;
        String reply = AppStore.buildSuccessMessage(context, req.amount, card.code);
        boolean effectiveRewards = AppStore.isRewardsEnabled(context) && agent.rewardsEnabled;
        RewardResult reward = null;
        if (effectiveRewards) {
            reward = AppStore.applyRewardsForPaidSale(context, customerPhone, agentName, req.amount, card.code);
            if (reward != null && reward.customerMessagePart != null && !reward.customerMessagePart.isEmpty()) reply += reward.customerMessagePart;
        }
        String note = "تم تنفيذ طلب رقم موثوق: " + agentName + " | السقف: " + agent.creditLimit + " | سيخصم من السقف بعد نجاح إرسال SMS | المستخدم المتوقع بعد النجاح: " + usedAfter;
        if (reward != null && reward.internalNote != null && !reward.internalNote.isEmpty()) note += "\n" + reward.internalNote;
        if (!effectiveRewards) note += "\nالمكافأة معطلة لهذا الرقم الموثوق؛ تم تنفيذ البيع بدون احتساب نقاط جديدة.";
        String logId = addLog(context, provider, sender, agentName, customerPhone, req.amount, "جاري إرسال SMS", note, card.code);
        sendSmsWithTracking(context, customerPhone, reply, logId, req.amount, card.code, false,
                "تم إرسال الكرت لزبون الرقم الموثوق وتم خصم العملية من السقف",
                "فشل إرسال SMS لزبون الرقم الموثوق؛ لم يتم خصم السقف. استخدم إعادة إرسال SMS أو واتساب",
                agent.id, req.amount);
        AppStore.performAutoBackupIfDue(context, "طلب رقم موثوق");
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

    static String cleanPhone(String phone) {
        return PaymentParser.normalizeLocalPhone(phone);
    }

    static boolean isValidLocalMobile(String phone) {
        return PaymentParser.hasValidLocalMobile(phone);
    }

    static boolean sendSmsWithTracking(Context context, String phone, String text, String logId, int amount, String cardCode, boolean noStock, String successMsg, String failMsg) {
        return sendSmsWithTracking(context, phone, text, logId, amount, cardCode, noStock, successMsg, failMsg, "", 0);
    }

    static boolean sendSmsWithTracking(Context context, String phone, String text, String logId, int amount, String cardCode, boolean noStock, String successMsg, String failMsg, String trustedCreditAgentId, int trustedCreditAmount) {
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
