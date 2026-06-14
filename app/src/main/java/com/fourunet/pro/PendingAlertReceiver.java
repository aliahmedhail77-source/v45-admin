package com.fourunet.pro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PendingAlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotifyHelper.notifyPendingReminder(context);
    }
}
