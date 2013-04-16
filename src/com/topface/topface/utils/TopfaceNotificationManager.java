package com.topface.topface.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.widget.RemoteViews;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.views.ImageViewRemote;

public class TopfaceNotificationManager {
    private static TopfaceNotificationManager mInstance;
    public static final int NOTIFICATION_ID = 1312; //Completely random number
    public static final int PROGRESS_ID = 1313;
    private float width = 64;
    private float height = 64;
    private Context ctx;

    private static int lastId = 1314;

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

    public int showNotification(String title, String message, Bitmap icon, int unread, Intent intent, boolean doNeedReplace) {

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

        int id = NOTIFICATION_ID;
        if (doNeedReplace) {
            id = ++lastId;
        }

        PendingIntent resultPendingIntent;

        if (TextUtils.equals(intent.getComponent().getClassName(), ContainerActivity.class.getName())) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
            // Adds the back stack
            stackBuilder.addParentStack(ContainerActivity.class);
            stackBuilder.editIntentAt(0).putExtra(GCMUtils.GCM_INTENT, true);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(intent);
            // Gets a PendingIntent containing the entire back stack
            resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        notificationBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection deprecation
        notificationManager.notify(id, notificationBuilder.getNotification());
        return id;
    }

    public int showProgressNotification(String title, String message, Bitmap icon, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return showNotificationForOldVersions(title, icon, intent);
        } else {
            return showNotificationsForNewVersions(title, message, icon, intent);
        }
    }


    private int showNotificationsForNewVersions(String title, String message, Bitmap icon, Intent intent) {
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
        notificationBuilder.setProgress(0, 0, true);


        PendingIntent resultPendingIntent;

        if (!TextUtils.equals(intent.getComponent().getClassName(), NavigationActivity.class.toString())) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
            // Adds the back stack
            stackBuilder.addParentStack(NavigationActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(intent);
            // Gets a PendingIntent containing the entire back stack
            resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notificationBuilder.setContentIntent(resultPendingIntent);

        int id = ++lastId;

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        //noinspection deprecation
        notificationManager.notify(id, notificationBuilder.build());
        return id;
    }

    private int showNotificationForOldVersions(String title, Bitmap icon, Intent intent) {
        int id = ++lastId;
        try {
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

            PendingIntent resultPendingIntent;

            if (!TextUtils.equals(intent.getComponent().getClassName(), NavigationActivity.class.toString())) {
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(ctx);
                // Adds the back stack
                stackBuilder.addParentStack(NavigationActivity.class);
                // Adds the Intent to the top of the stack
                stackBuilder.addNextIntent(intent);
                // Gets a PendingIntent containing the entire back stack
                resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
            } else {
                resultPendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            notificationBuilder.setContentIntent(resultPendingIntent);
            Notification not = notificationBuilder.build();

            not.contentView = views;


            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            //noinspection deprecation
            notificationManager.notify(PROGRESS_ID, not);
        } catch (Exception e) {
            Debug.error(e);
        }
        return id;
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
