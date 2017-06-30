package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.statistics.NotificationStatistics;
import com.topface.topface.utils.gcmutils.GCMUtils;

/**
 * This receiver handles notification remove.
 */
public class NotificationClosedReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_CLOSED = "com.topface.topface.NOTIFICATION_CLOSED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            int type = intent.getIntExtra(GCMUtils.GCM_TYPE, -1);
            NotificationStatistics.sendDeleted(type, intent.getStringExtra(GCMUtils.GCM_LABEL));
            Debug.log("Notification deleted" + type + " " +
                    intent.getStringExtra(GCMUtils.GCM_LABEL));
            GCMUtils.cancelNotification(type);
        }
    }
}