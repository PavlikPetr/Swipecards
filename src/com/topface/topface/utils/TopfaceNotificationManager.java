package com.topface.topface.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import com.topface.topface.R;

public class TopfaceNotificationManager {
    private static TopfaceNotificationManager mInstance;
    public static final int id = 1312; //Completely random number
    private float width = 64;
    private float height = 64;
    private Context ctx;

    public static TopfaceNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TopfaceNotificationManager(context);
        }
        return mInstance;
    }

    private TopfaceNotificationManager(Context context) {
        float scale = context.getResources().getDisplayMetrics().density;

        width *= scale;
        height *= scale;
        ctx = context;
    }

    public void showNotification(String title, String message, Bitmap icon, int unread, Intent intent) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx);
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);

        if (icon != null) {
            Bitmap scaledIcon = Utils.clipAndScaleBitmap(icon, (int) width, (int) height);
            if (scaledIcon != null) {
                notificationBuilder.setLargeIcon(scaledIcon);
            }
        }

        if (Settings.getInstance().isVibrationEnabled()) {
            notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        }

        notificationBuilder.setSound(Settings.getInstance().getRingtone());
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setAutoCancel(true);

        if (unread > 0) {
            notificationBuilder.setNumber(unread);
        }

        PendingIntent resultPendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection deprecation
        notificationManager.notify(id, notificationBuilder.getNotification());
    }
}
