package com.topface.topface.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.topface.topface.R;

public class TopfaceNotificationManager {
    private static TopfaceNotificationManager mInstance;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    public static final int id = 1312; //Completely random number
    private float scale = 0;
    private float width = 64;
    private float height = 64;
    private TaskStackBuilder stackBuilder;

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
        ctx = context;
        stackBuilder = TaskStackBuilder.create(context);
    }

    public void showNotification(int userId, String title, String message, Bitmap icon, int unread, Intent intent) {    	
    	
//        stackBuilder.addNextIntent(intent);//addNextIntent(intent);

        Bitmap scaledIcon = Utils.clipAndScaleBitmap(icon,(int)width,(int)height);

        mNotificationBuilder.setContentTitle(title);
        mNotificationBuilder.setContentText(message);
        mNotificationBuilder.setLargeIcon(scaledIcon);
        if(unread>0)
            mNotificationBuilder.setNumber(unread);
        mNotificationBuilder.setAutoCancel(true);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager.notify(id, mNotificationBuilder.getNotification());
    }

    @SuppressWarnings("unused")
	private Context ctx;
}
