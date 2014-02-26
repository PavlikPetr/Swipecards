package com.topface.topface.utils.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.TextUtils;

import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class UserNotification {
    public static final int ICON_SIZE = 64;
    public static final String NOTIFICATION_ID = "notification_id";
    private Bitmap mImage;
    private String mText;
    private String mTitle;

    private int mId;
    private boolean mOngoing;


    private MessageStack messages;

    public enum Type {PROGRESS, STANDARD, FAIL, ACTIONS}

    private Type mType;
    private boolean mIsTextNotification;

    private int unread = 0;
    private Context mContext;
    private NotificationCompat.Builder notificationBuilder;

    public UserNotification(Context context) {
        this.mContext = context;
    }

    public void setType(Type mType) {
        this.mType = mType;
    }

    public void setImage(Bitmap image) {
        this.mImage = image;
    }

    public void setIsTextNotification(boolean isTextNotification) {
        this.mIsTextNotification = isTextNotification;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public void setOngoing(boolean mOngoing) {
        this.mOngoing = mOngoing;
    }

    public static int getIconSize(Context context) {
        return (int) (context.getResources().getDisplayMetrics().density * ICON_SIZE);
    }

    public void setMessages(MessageStack messages) {
        this.messages = messages;
    }

    public android.app.Notification generate(Intent intent, NotificationAction[] actions) {
        try {
            notificationBuilder = new NotificationCompat.Builder(mContext);
            if (Settings.getInstance().isVibrationEnabled()) {
                notificationBuilder.setDefaults(android.app.Notification.DEFAULT_VIBRATE);
            }
            notificationBuilder.setSound(Settings.getInstance().getRingtone());
            notificationBuilder.setOngoing(mOngoing);
            switch (mType) {
                case PROGRESS:
                    return generateProgress(intent);
                case FAIL:
                    return generateFail(intent);
                case STANDARD:
                    return generateStandard(intent);
                case ACTIONS:
                    return generateWithActions(actions);
            }
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }

    private android.app.Notification generateStandard(Intent intent) {
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        setLargeIcon();
        notificationBuilder.setContentTitle(mTitle);
        notificationBuilder.setContentText(mText);
        if (mIsTextNotification) {
            if (messages.size() <= 1) {
                generateBigText();
            } else {
                generateInbox();
            }
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
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setContentTitle(mTitle);
        notificationBuilder.setContentText(mText);
        notificationBuilder.setAutoCancel(true);
        setLargeIcon();
        Intent retryIntent = new Intent(AddPhotoHelper.CANCEL_NOTIFICATION_RECEIVER + intent.getParcelableExtra("PhotoUrl"));
        retryIntent.putExtra("id", mId);
        retryIntent.putExtra("isRetry", true);
        PendingIntent resultPendingIntent = generatePendingIntent(intent);
        notificationBuilder.setContentIntent(resultPendingIntent);
        return notificationBuilder.build();
    }

    private android.app.Notification generateProgress(Intent intent) {
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
        setLargeIcon();
        notificationBuilder.setContentTitle(mTitle);
        PendingIntent resultPendingIntent = generatePendingIntent(intent);
        generateBigPicture();
        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationBuilder.setProgress(100, 100, true);
        return notificationBuilder.build();
    }

    public android.app.Notification generateWithActions(NotificationAction[] actions) {
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setContentTitle(mTitle)
                .setContentText(mText)
                .setOngoing(mOngoing);
        if (actions != null) {
            for (NotificationAction action : actions) {
                notificationBuilder.addAction(action.iconResId, action.text, action.intent);
            }
        }
        return notificationBuilder.build();
    }

    private void generateBigPicture() {
        NotificationCompat.BigPictureStyle inboxStyle =
                new NotificationCompat.BigPictureStyle(notificationBuilder.setContentTitle(mTitle));
        Drawable blankDrawable = mContext.getResources().getDrawable(R.drawable.ic_notification);
        if (blankDrawable != null) {
            Bitmap blankBitmap = ((BitmapDrawable) blankDrawable).getBitmap();
            inboxStyle.bigLargeIcon(blankBitmap);
        }
        inboxStyle.bigPicture(mImage).build();
    }

    private void generateBigText() {
        NotificationCompat.BigTextStyle bigTextStyle =
                new NotificationCompat.BigTextStyle(notificationBuilder.setContentTitle(mTitle));

        bigTextStyle.bigText(mText).build();
    }

    private void generateInbox() {
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle(notificationBuilder.setContentTitle(
                        Utils.getQuantityString(R.plurals.notification_many_messages,
                                messages.getAllCount(), messages.getAllCount())));
        for (Spannable message : messages) {
            inboxStyle.addLine(message);
        }
        inboxStyle.build();
    }

    private PendingIntent generatePendingIntent(Intent intent) {
        PendingIntent resultPendingIntent;
        intent.putExtra(NOTIFICATION_ID, mId);
        if (!TextUtils.equals(intent.getComponent().getClassName(), NavigationActivity.class.toString())) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            // Adds the back stack
            stackBuilder.addParentStack(NavigationActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(intent);
            // Gets a PendingIntent containing the entire back stack
            resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return resultPendingIntent;
    }

    private void setLargeIcon() {
        if (mImage != null) {
            Bitmap scaledIcon = Utils.clipAndScaleBitmap(mImage, getIconSize(mContext), getIconSize(mContext));
            if (scaledIcon != null) {
                notificationBuilder.setLargeIcon(scaledIcon);
            } else {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.ic_notification));
            }
        } else {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
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