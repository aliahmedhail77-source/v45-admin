package com.fourunet.pro;

import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null) return;

        // STAGE 13.6.2 HOTFIX:
        // Keep the SMS broadcast alive while we hand the message to the internal FIFO worker.
        // On some devices, returning immediately while the app is under memory pressure can stop
        // automatic card processing before the worker starts.
        final PendingResult pendingResult = goAsync();
        final Context appContext = context.getApplicationContext() == null ? context : context.getApplicationContext();
        final Bundle bundle = intent.getExtras();

        new Thread(() -> {
            try {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");
                if (pdus == null) return;

                String sender = "";
                StringBuilder body = new StringBuilder();
                long receivedAt = 0L;

                for (Object pdu : pdus) {
                    SmsMessage msg;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        msg = SmsMessage.createFromPdu((byte[]) pdu, format);
                    } else {
                        msg = SmsMessage.createFromPdu((byte[]) pdu);
                    }
                    if (msg == null) continue;
                    sender = msg.getDisplayOriginatingAddress();
                    if (receivedAt == 0L) receivedAt = msg.getTimestampMillis();
                    body.append(msg.getMessageBody());
                }

                SmsProcessor.enqueueIncomingSms(appContext, sender, body.toString(), receivedAt);
            } finally {
                try { pendingResult.finish(); } catch (Exception ignored) {}
            }
        }).start();
    }
}
