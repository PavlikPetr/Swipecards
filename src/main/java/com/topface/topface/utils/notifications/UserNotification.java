package com.topface.topface.utils.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.facebook.topface.Util;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.Utils;

public class UserNotification {
    public static final int ICON_SIZE = 64;
    private Bitmap image;
    private String text;
    private String title;

    private int id;
    private boolean ongoing;

    public enum Type {PROGRESS, STANDARD, FAIL, ACTIONS}

    private Type type;
    private boolean isTextNotification;

    private int unread = 0;
    private Context context;
    private NotificationCompat.Builder notificationBuilder;

    public UserNotification(Context context) {
        this.context = context;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setIsTextNotification(boolean isTextNotification) {
        this.isTextNotification = isTextNotification;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public static int getIconSize(Context context) {
        return (int)(context.getResources().getDisplayMetrics().density * ICON_SIZE);
    }

    public android.app.Notification generate(Intent intent, NotificationAction[] actions) {
        notificationBuilder = new NotificationCompat.Builder(context);
        if (Settings.getInstance().isVibrationEnabled()) {
            notificationBuilder.setDefaults(android.app.Notification.DEFAULT_VIBRATE);
        }
        notificationBuilder.setSound(Settings.getInstance().getRingtone());
        notificationBuilder.setOngoing(ongoing);
        switch (type) {
            case PROGRESS:
                return generateProgress(intent);
            case FAIL:
                return generateFail(intent);
            case STANDARD:
                return generateStandard(intent);
            case ACTIONS:
                return generateWithActions(actions);
        }
        return null;
    }

    @SuppressWarnings("UnusedDeclaration")
    private android.app.Notification generateStandard(Intent intent) {
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        setLargeIcon();
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(text);
        if (isTextNotification) {
            generateBigText();
        } else {
            generateBigPicture();
        }
        if (unread > 0) {
            notificationBuilder.setNumber(unread);
        }
        PendingIntent resultPendingIntent = generatePendingIntent(intent);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(resultPendingIntent);
        //noinspection deprecation
        return notificationBuilder.build();
    }

    private android.app.Notification generateFail(Intent intent) {
        try {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification);
            notificationBuilder.setContentTitle(title);
            notificationBuilder.setContentText(text);
            setLargeIcon();
            Intent retryIntent = new Intent(AddPhotoHelper.CANCEL_NOTIFICATION_RECEIVER + intent.getParcelableExtra("PhotoUrl"));
            retryIntent.putExtra("id", id);
            retryIntent.putExtra("isRetry", true);
            PendingIntent resultPendingIntent = generatePendingIntent(intent);
            notificationBuilder.setContentIntent(resultPendingIntent);
            return notificationBuilder.build();
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }

    private android.app.Notification generateProgress(Intent intent) {
        try {
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
            setLargeIcon();
            notificationBuilder.setContentTitle(title);
            PendingIntent resultPendingIntent = generatePendingIntent(intent);
            generateBigPicture();
            notificationBuilder.setContentIntent(resultPendingIntent);
            notificationBuilder.setProgress(100,100,true);
            android.app.Notification not = notificationBuilder.build();
            return not;
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }

    public android.app.Notification generateWithActions(NotificationAction[] actions) {

        notificationBuilder.setContentTitle(title)
                .setContentText(text)
                .setOngoing(ongoing);
        if (actions != null) {
            for (NotificationAction action : actions) {
                notificationBuilder.addAction(action.iconResId, action.text, action.intent);
            }
        }
        return notificationBuilder.build();
    }

    private void generateBigPicture() {
        NotificationCompat.BigPictureStyle inboxStyle =
                new NotificationCompat.BigPictureStyle(notificationBuilder.setContentTitle(title));
        Drawable blankDrawable = context.getResources().getDrawable(R.drawable.ic_notification);
        Bitmap blankBitmap=((BitmapDrawable)blankDrawable).getBitmap();
        inboxStyle.bigLargeIcon(blankBitmap);
        inboxStyle.bigPicture(image).build();
    }


    private PendingIntent generateBigPicture(Intent cancelIntent, String name) {
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, cancelIntent
                , PendingIntent.FLAG_CANCEL_CURRENT);


        Bitmap tficon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_notification);

        NotificationCompat.BigPictureStyle inboxStyle =
                new NotificationCompat.BigPictureStyle(notificationBuilder.setLargeIcon(tficon).setContentTitle(title).addAction(0, name, pi));

        inboxStyle.bigPicture(image).build();

        return pi;
    }

    private void generateBigText() {
        NotificationCompat.BigTextStyle inboxStyle =
                new NotificationCompat.BigTextStyle(notificationBuilder.setContentTitle(title));

        inboxStyle.bigText(text).build();
    }

    @SuppressWarnings("UnusedDeclaration")
    private void generateBigText(RemoteViews views, Intent cancelIntent, String name) {
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, cancelIntent
                , PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.notCancelButton, pi);
        NotificationCompat.BigTextStyle inboxStyle =
                new NotificationCompat.BigTextStyle(notificationBuilder.addAction(0, name, pi));

        inboxStyle.bigText(text).build();
    }

    private PendingIntent generatePendingIntent(Intent intent) {
        PendingIntent resultPendingIntent;

        if (!TextUtils.equals(intent.getComponent().getClassName(), NavigationActivity.class.toString())) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack
            stackBuilder.addParentStack(NavigationActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(intent);
            // Gets a PendingIntent containing the entire back stack
            resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return resultPendingIntent;
    }

    private void setLargeIcon() {
        if (image != null) {
            Bitmap scaledIcon = Utils.clipAndScaleBitmap(image, getIconSize(context), getIconSize(context));//Utils.clipBitmap(image);
            if (scaledIcon != null) {
                notificationBuilder.setLargeIcon(scaledIcon);
            } else {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_notification));
            }
        } else {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_notification));
        }
    }

    public static class NotificationAction {
        int iconResId;
        String text;
        PendingIntent intent;

        public NotificationAction(int iconResId, String text, PendingIntent intent) {
            this.iconResId = iconResId;
            this.text = text;
            this.intent = intent;
        }
    }
}