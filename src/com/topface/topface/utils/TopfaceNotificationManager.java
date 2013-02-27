package com.topface.topface.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.topface.topface.R;
import com.topface.topface.ui.views.ImageViewRemote;

public class TopfaceNotificationManager {
    private static TopfaceNotificationManager mInstance;
    public static final int id = 1312; //Completely random number
    public static final int PROGRESS_ID = 1313;
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

    public void showProgressNotification(String title, String message, Bitmap icon, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showNotificationForOldVersions(title, message, icon, intent);
        } else {
            showNotificationsForNewVersions(title, message, icon, intent);
        }
    }



    private void showNotificationsForNewVersions(String title, String message, Bitmap icon, Intent intent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);

        if (icon != null) {
            Bitmap scaledIcon = Utils.clipAndScaleBitmap(icon, (int) width, (int) height);
            if (scaledIcon != null) {
                notificationBuilder.setLargeIcon(scaledIcon);
            }
        }

        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(message);
        notificationBuilder.setProgress(0,0,true);


        PendingIntent resultPendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);


        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection deprecation
        notificationManager.notify(PROGRESS_ID, notificationBuilder.build());
    }

    private void showNotificationForOldVersions(String title, String message, Bitmap icon, Intent intent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(ctx);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
        RemoteViews views = new RemoteViews(ctx.getPackageName(), R.layout.notifications_progress_layout);
        if (icon != null) {
            Bitmap scaledIcon = Utils.clipAndScaleBitmap(icon, (int) width, (int) height);
            if (scaledIcon != null) {
                views.setBitmap(R.id.notificationImage, "setImageBitmap", icon);
            }
        }

        views.setTextViewText(R.id.nfTitle, title);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        Notification not = notificationBuilder.build();

        not.contentView = views;

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection deprecation
        notificationManager.notify(PROGRESS_ID, not);
    }

    public void cancelNotification(int id) {
        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    public static class TempImageViewRemote extends ImageViewRemote {
        private Bitmap mImageBitmap;

        public TempImageViewRemote(Context context) {
            super(context);
        }

        @Override
        public void setImageBitmap(Bitmap bm) {
            super.setImageBitmap(bm);
            mImageBitmap = bm;
        }

        public Bitmap getImageBitmap() {
            return mImageBitmap;
        }
    }
}
