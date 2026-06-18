package com.fourunet.pro;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import java.util.ArrayList;

public class SmsStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String logId = intent.getStringExtra("logId");
        String phone = intent.getStringExtra("phone");
        int amount = intent.getIntExtra("amount", 0);
        String cardCode = intent.getStringExtra("cardCode");
        boolean noStock = intent.getBooleanExtra("noStock", false);
        int part = intent.getIntExtra("part", 1);
        int total = intent.getIntExtra("total", 1);
        String successMsg = intent.getStringExtra("successMsg");
        String failMsg = intent.getStringExtra("failMsg");
        String trustedCreditAgentId = intent.getStringExtra("trustedCreditAgentId");
        int trustedCreditAmount = intent.getIntExtra("trustedCreditAmount", 0);
        String trustedNotifyPhone = intent.getStringExtra("trustedNotifyPhone");
        String trustedNotifyText = intent.getStringExtra("trustedNotifyText");

        int result = getResultCode();
        if (result == Activity.RESULT_OK) {
            // في الرسائل الطويلة ننتظر آخر جزء حتى لا نكرر الإشعارات.
            if (part >= total) {
                if (trustedCreditAgentId != null && !trustedCreditAgentId.trim().isEmpty() && trustedCreditAmount > 0 && !noStock) {
                    AppStore.addTrustedCreditUsage(context, trustedCreditAgentId, trustedCreditAmount);
                }
                AppStore.updateLogStatus(context, logId, "تم إرسال SMS", successMsg == null ? "تم إرسال الرسالة للزبون" : successMsg);
                sendTrustedConfirmationIfNeeded(trustedNotifyPhone, trustedNotifyText);
                if (noStock) NotifyHelper.notifyNoStockSent(context, amount, phone);
                else NotifyHelper.notifyCardSold(context, amount, cardCode == null ? "" : cardCode, phone);
            }
        } else {
            String reason = smsFailureReason(result);
            AppStore.updateLogStatus(context, logId, "فشل إرسال SMS", (failMsg == null ? "فشل إرسال الرسالة" : failMsg) + "\nسبب النظام: " + reason);
            if (noStock) NotifyHelper.notifyNoStockFailed(context, amount, phone);
            else NotifyHelper.notifySendFailed(context, amount, cardCode == null ? "" : cardCode, phone);
        }
    }

    private void sendTrustedConfirmationIfNeeded(String phone, String text) {
        try {
            String clean = PaymentParser.normalizeLocalPhone(phone);
            if (!PaymentParser.hasValidLocalMobile(clean)) return;
            if (text == null || text.trim().isEmpty()) return;
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> parts = sms.divideMessage(text);
            sms.sendMultipartTextMessage(clean, null, parts, null, null);
        } catch (Exception ignored) {}
    }

    private String smsFailureReason(int result) {
        switch (result) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "فشل عام؛ افحص الرصيد أو الشبكة أو شريحة SMS الافتراضية";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "لا توجد خدمة شبكة";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "خطأ داخلي في الرسالة";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "الشبكة/الراديو مغلق";
            default:
                return "كود خطأ: " + result;
        }
    }
}
