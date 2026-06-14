package com.fourunet.pro;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

class NotifyHelper {
    private static final String CHANNEL_ID = "card_sales";
    private static final String CHANNEL_NAME = "إشعارات بيع الكروت";
    private static final String ALERT_CHANNEL_ID = "card_sales_pending_alerts_v2";
    private static final String ALERT_CHANNEL_NAME = "تنبيهات الطابور المعلّق";

    static void notifyCardSold(Context context, int amount, String code, String phone) {
        String title = "تم بيع كرت فئة " + amount + " ريال";
        String body = "رقم الكرت: " + code + "\nالعميل: " + (phone == null || phone.isEmpty() ? "-" : phone) + "\nالشبكة: فور يو نت";
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
        String title = "🚨 كرت معلّق يحتاج مراجعة";
        String body = (amount > 0 ? "الفئة: " + amount + " ريال\n" : "")
                + (code == null || code.isEmpty() ? "" : "الكرت: " + code + "\n")
                + "السبب: " + (reason == null || reason.trim().isEmpty() ? "عملية معلّقة" : reason)
                + "\nسيتم تذكيرك كل دقيقة ما دام هناك سجل معلّق.";
        show(context, title, body, true);
        schedulePendingReviewAlert(context);
    }

    static void notifyPendingReminder(Context context) {
        if (context == null) return;
        if (!AppStore.hasPendingCriticalLogs(context)) {
            cancelPendingReviewAlert(context);
            return;
        }
        show(context, "🚨 توجد عمليات كروت معلّقة", "راجع السجلات: يوجد كرت/تحويل معلّق بسبب رقم غير معروف أو نفاد فئة.", true);
        schedulePendingReviewAlert(context);
    }

    static void schedulePendingReviewAlert(Context context) {
        try {
            if (context == null) return;
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

    private static void show(Context context, String title, String body) {
        show(context, title, body, false);
    }

    private static void show(Context context, String title, String body, boolean urgent) {
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(urgent ? ALERT_CHANNEL_ID : CHANNEL_ID, urgent ? ALERT_CHANNEL_NAME : CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                ch.enableVibration(true);
                nm.createNotificationChannel(ch);
            }

            Intent open = new Intent(context, MainActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int flags = Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
            PendingIntent pi = PendingIntent.getActivity(context, 0, open, flags);

            Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(context, urgent ? ALERT_CHANNEL_ID : CHANNEL_ID)
                    : new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(body.replace("\n", "  "))
                    .setStyle(new Notification.BigTextStyle().bigText(body))
                    .setAutoCancel(!urgent)
                    .setOngoing(urgent)
                    .setContentIntent(pi)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
            if (Build.VERSION.SDK_INT >= 21) builder.setColor(urgent ? 0xffff4081 : 0xff6d4bb3);

            nm.notify((int) (System.currentTimeMillis() % Integer.MAX_VALUE), builder.build());
        } catch (Exception ignored) {}
    }
}
