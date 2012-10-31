package com.topface.topface.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;

import java.util.logging.StreamHandler;

public class TopfaceNotificationManager {
    private static TopfaceNotificationManager mInstance;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private int count = 0;
    private int id = 1312; //Completely random number
    private float scale = 0;
    float width = 64;
    float height = 64;

    public static TopfaceNotificationManager getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new TopfaceNotificationManager(context);
        }
        return mInstance;
    }

    private TopfaceNotificationManager(Context context) {
        mNotificationBuilder = new NotificationCompat.Builder(context);

        if(Settings.getInstance().isVibrationEnabled())
            mNotificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        mNotificationBuilder.setSound(RingtoneManager.getActualDefaultRingtoneUri(context,RingtoneManager.TYPE_NOTIFICATION));
        mNotificationBuilder.setSmallIcon(R.drawable.ic_notification);

        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        scale = context.getResources().getDisplayMetrics().density;

        width *= scale;
        height *= scale;
    }

    public void showNotification(String title, String message, Activity parentActivity, Bitmap icon) {
        count++;

        Bitmap scaledIcon = Utils.clipAndScaleBitmap(icon,(int)width,(int)height);

        mNotificationBuilder.setContentTitle(title);
        mNotificationBuilder.setContentText(message);
        mNotificationBuilder.setLargeIcon(scaledIcon);
        mNotificationBuilder.setNumber(count);
        mNotificationBuilder.setAutoCancel(true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(parentActivity);
        stackBuilder.addParentStack(parentActivity);
        stackBuilder.addNextIntent(new Intent(parentActivity, NavigationActivity.class));

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(id, mNotificationBuilder.getNotification());
    }
}
