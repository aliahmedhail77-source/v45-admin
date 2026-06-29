package com.fourunet.pro;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

class NotifyHelper {
    private static final String CHANNEL_ID = "card_sales";
    private static final String CHANNEL_NAME = "إشعارات بيع الكروت";
    private static final String ALERT_CHANNEL_ID = "card_sales_pending_alerts_v4";
    private static final String ALERT_CHANNEL_NAME = "تنبيهات العمليات التي تحتاج مراجعة";
    private static final String DEPOSIT_ALERT_CHANNEL_ID = "wallet_explicit_deposit_alerts_v1";
    private static final String DEPOSIT_ALERT_CHANNEL_NAME = "تنبيهات الإيداع الصريح غير المنفذ";
    private static final int PENDING_REMINDER_NOTIFICATION_ID = 909002;

    static void notifyCardSold(Context context, int amount, String code, String phone) {
        String title = "تم بيع كرت فئة " + amount + " ريال";
        String body = "رقم الكرت: " + code + "\nالعميل: " + (phone == null || phone.isEmpty() ? "-" : phone) + "\nالشبكة: " + AppStore.getNetworkName(context);
        show(context, title, body);
    }

    static void notifySendFailed(Context context, int amount, String code, String phone) {
        String title = "فشل إرسال كرت فئة " + amount + " ريال";
        String body = "تم حجز الكرت: " + code + "\nالعميل: " + (phone == null || phone.isEmpty() ? "-" : phone) + "\nراجع السجل لإعادة المعالجة يدويًا.";
        show(context, title, body);
    }

    static void notifyNoStockSent(Context context, int amount, String phone) {
        String title = "نفدت كروت فئة " + amount + " ريال";
        String body = "تم استلام السداد، ولا توجد كروت متاحة. تم إرسال تنبيه نفاد الفئة إلى العميل: " + (phone == null || phone.isEmpty() ? "-" : phone);
        show(context, title, body);
    }

    static void notifyNoStockFailed(Context context, int amount, String phone) {
        String title = "فشل تنبيه نفاد فئة " + amount + " ريال";
        String body = "لا توجد كروت متاحة، ولم يتم إرسال رسالة التنبيه إلى العميل: " + (phone == null || phone.isEmpty() ? "-" : phone) + "\nراجع صلاحية إرسال الرسائل ورصيد الشريحة.";
        show(context, title, body);
    }

    static void notifyPendingAction(Context context, int amount, String code, String reason) {
        notifyPendingAction(context, "", amount, code, reason);
    }

    static void notifyPendingAction(Context context, String logId, int amount, String code, String reason) {
        String title = "🚨 عملية تحتاج مراجعة";
        String body = (amount > 0 ? "الفئة: " + amount + " ريال\n" : "")
                + (code == null || code.isEmpty() ? "" : "الكرت: " + code + "\n")
                + "السبب: " + (reason == null || reason.trim().isEmpty() ? "عملية معلّقة" : reason)
                + "\nاضغط على الإشعار لفتح العملية نفسها، ثم اضغط تمت المراجعة عند الانتهاء.";
        showReview(context, title, body, logId, notificationIdForLog(logId));
        schedulePendingReviewAlert(context);
    }

    static void notifyExplicitDepositReview(Context context, String logId, int amount, String walletName, String customerName, String reason) {
        String title = "🔴 إيداع صريح غير منفذ";
        String body = "المحفظة: " + (walletName == null || walletName.trim().isEmpty() ? "محفظة" : walletName.trim())
                + (amount > 0 ? "\nالمبلغ: " + amount + " ريال" : "")
                + (customerName == null || customerName.trim().isEmpty() ? "" : "\nالاسم: " + customerName.trim())
                + "\nالسبب: " + (reason == null || reason.trim().isEmpty() ? "تحتاج مراجعة" : reason.trim())
                + "\nاضغط لفتح العملية، ثم اضغط تمت المراجعة لإيقاف التنبيه.";
        showDepositReview(context, title, body, logId, notificationIdForLog(logId));
        schedulePendingReviewAlert(context);
    }

    static void notifyPendingReminder(Context context) {
        if (context == null) return;
        OperationLog first = AppStore.firstPendingCriticalLogForReminder(context);
        if (first == null) {
            cancelPendingReviewAlert(context);
            cancelReminderNotification(context);
            return;
        }
        String body = "توجد عملية واحدة أو أكثر تحتاج مراجعة.\n"
                + "العملية الأقرب: " + first.amount + " ريال - " + (first.customerPhone == null || first.customerPhone.isEmpty() ? "بدون رقم" : first.customerPhone)
                + (first.customerName == null || first.customerName.trim().isEmpty() ? "" : "\nالاسم: " + first.customerName.trim())
                + "\nاضغط هنا لفتح العملية المميزة بالأحمر.";
        if (AppStore.isExplicitDepositReviewLog(first)) {
            showDepositReview(context, "🔴 إيداع صريح بانتظار المراجعة", body, first.id, PENDING_REMINDER_NOTIFICATION_ID);
        } else {
            showReview(context, "🚨 عمليات تحتاج مراجعة", body, first.id, PENDING_REMINDER_NOTIFICATION_ID);
        }
        schedulePendingReviewAlert(context);
    }

    static void schedulePendingReviewAlert(Context context) {
        try {
            if (context == null) return;
            if (AppStore.firstPendingCriticalLogForReminder(context) == null) return;
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;
            Intent intent = new Intent(context, PendingAlertReceiver.class);
            int flags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
            PendingIntent pi = PendingIntent.getBroadcast(context, 909001, intent, flags);
            long when = System.currentTimeMillis() + 60_000L;
            if (Build.VERSION.SDK_INT >= 23) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when, pi);
            else am.set(AlarmManager.RTC_WAKEUP, when, pi);
        } catch (Exception ignored) {}
    }

    static void cancelPendingReviewAlert(Context context) {
        try {
            if (context == null) return;
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am == null) return;
            Intent intent = new Intent(context, PendingAlertReceiver.class);
            int flags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
            PendingIntent pi = PendingIntent.getBroadcast(context, 909001, intent, flags);
            am.cancel(pi);
        } catch (Exception ignored) {}
    }

    static void cancelReviewNotification(Context context, String logId) {
        try {
            if (context == null) return;
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            nm.cancel(notificationIdForLog(logId));
            if (!AppStore.hasPendingCriticalLogs(context)) {
                nm.cancel(PENDING_REMINDER_NOTIFICATION_ID);
                cancelPendingReviewAlert(context);
            }
        } catch (Exception ignored) {}
    }

    static void cancelReminderNotification(Context context) {
        try {
            if (context == null) return;
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(PENDING_REMINDER_NOTIFICATION_ID);
        } catch (Exception ignored) {}
    }

    private static int notificationIdForLog(String logId) {
        if (logId == null || logId.trim().isEmpty()) return PENDING_REMINDER_NOTIFICATION_ID;
        return 910000 + Math.abs(logId.hashCode() % 80000);
    }

    private static void show(Context context, String title, String body) {
        showInternal(context, title, body, false, false, null, (int) (System.currentTimeMillis() % Integer.MAX_VALUE), true, false);
    }

    private static void showReview(Context context, String title, String body, String logId, int notificationId) {
        showInternal(context, title, body, true, false, logId, notificationId, true, false);
    }

    private static void showDepositReview(Context context, String title, String body, String logId, int notificationId) {
        showInternal(context, title, body, true, true, logId, notificationId, true, false);
    }

    private static void showInternal(Context context, String title, String body, boolean urgent, boolean depositAlert, String logId, int notificationId, boolean autoCancel, boolean ongoing) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String channelId = depositAlert ? DEPOSIT_ALERT_CHANNEL_ID : (urgent ? ALERT_CHANNEL_ID : CHANNEL_ID);
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(channelId, depositAlert ? DEPOSIT_ALERT_CHANNEL_NAME : (urgent ? ALERT_CHANNEL_NAME : CHANNEL_NAME), NotificationManager.IMPORTANCE_HIGH);
                ch.enableVibration(true);
                if (depositAlert) {
                    AudioAttributes attrs = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                    ch.setSound(alarmSound, attrs);
                }
                nm.createNotificationChannel(ch);
            }

            Intent open = new Intent(context, MainActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (logId != null && !logId.trim().isEmpty()) {
                open.putExtra("open_review_log_id", logId);
            } else if (urgent) {
                open.putExtra("open_pending_reviews", true);
            }
            int flags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
            int requestCode = notificationId <= 0 ? 0 : notificationId;
            PendingIntent pi = PendingIntent.getActivity(context, requestCode, open, flags);

            Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(context, channelId)
                    : new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(body.replace("\n", "  "))
                    .setStyle(new Notification.BigTextStyle().bigText(body))
                    .setAutoCancel(autoCancel)
                    .setOngoing(ongoing)
                    .setContentIntent(pi)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(depositAlert ? Notification.DEFAULT_VIBRATE : (Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE));
            if (depositAlert && Build.VERSION.SDK_INT < 26) builder.setSound(alarmSound);
            if (Build.VERSION.SDK_INT >= 21) builder.setColor(depositAlert ? 0xffff0033 : (urgent ? 0xffff4055 : 0xff6d4bb3));

            nm.notify(notificationId <= 0 ? (int) (System.currentTimeMillis() % Integer.MAX_VALUE) : notificationId, builder.build());
        } catch (Exception ignored) {}
    }
}
