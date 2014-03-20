package com.topface.topface.utils.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.topface.topface.R;
import com.topface.topface.data.SerializableToJson;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.Utils;

public class UserNotification {
    public static final int ICON_SIZE = 64;
    public static final String NOTIFICATION_ID = "notification_id";
    private Bitmap mImage;
    private String mText;
    private String mTitle;


    private Intent mIntent;
    private int mId;
    private boolean mOngoing;



    Notification generatedNotification;

    private MessageStack messages;
    private Type mType;
    private boolean mIsTextNotification;
    private int unread = 0;
    private Context mContext;
    private NotificationCompat.Builder notificationBuilder;

    public UserNotification(Context context) {
        this.mContext = context;
    }

    public static int getIconSize(Context context) {
        return (int) (context.getResources().getDisplayMetrics().density * ICON_SIZE);
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

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public int getId() {
        return mId;
    }

    public Notification getGeneratedNotification() {
        return generatedNotification;
    }

    public void setMessages(MessageStack messages) {
        this.messages = messages;
    }

    public Notification generate() {
        return generate(null);
    }

    public android.app.Notification generate(NotificationAction[] actions) {
        try {
            notificationBuilder = new NotificationCompat.Builder(mContext);
            if (Settings.getInstance().isVibrationEnabled()) {
                notificationBuilder.setDefaults(android.app.Notification.DEFAULT_VIBRATE);
            }
            notificationBuilder.setSound(Settings.getInstance().getRingtone());
            notificationBuilder.setOngoing(mOngoing);
            switch (mType) {
                case PROGRESS:
                    return generateProgress();
                case FAIL:
                    return generateFail();
                case STANDARD:
                    return generateStandard();
                case ACTIONS:
                    return generateWithActions(actions);
            }
        } catch (Exception e) {
            Debug.error(e);
        }
        return null;
    }

    private android.app.Notification generateStandard() {
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
        PendingIntent resultPendingIntent = generatePendingIntent(mIntent);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(resultPendingIntent);

        generatedNotification = notificationBuilder.build();
        return generatedNotification;
    }

    private android.app.Notification generateFail() {
        notificationBuilder.setSmallIcon(R.drawable.ic_notification);
        notificationBuilder.setContentTitle(mTitle);
        notificationBuilder.setContentText(mText);
        notificationBuilder.setAutoCancel(true);
        setLargeIcon();
        Intent retryIntent = new Intent(AddPhotoHelper.CANCEL_NOTIFICATION_RECEIVER + mIntent.getParcelableExtra("PhotoUrl"));
        retryIntent.putExtra("id", mId);
        retryIntent.putExtra("isRetry", true);
        PendingIntent resultPendingIntent = generatePendingIntent(mIntent);
        notificationBuilder.setContentIntent(resultPendingIntent);
        generatedNotification = notificationBuilder.build();
        return generatedNotification;
    }

    private android.app.Notification generateProgress() {
        notificationBuilder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
        notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
        notificationBuilder.setSound(Uri.EMPTY);
        setLargeIcon();
        notificationBuilder.setContentTitle(mTitle);
        PendingIntent resultPendingIntent = generatePendingIntent(mIntent);
        generateBigPicture();
        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationBuilder.setProgress(100, 100, true);
        if (isVersionOld()) {
            notificationBuilder.setContentText(mContext.getString(R.string.waiting_for_load));
        }
        generatedNotification = notificationBuilder.build();
        return generatedNotification;
    }

    public Notification updateProgress(int currentProgress) {
        notificationBuilder.setProgress(100, currentProgress, false);
        if (isVersionOld()) {
            notificationBuilder.setContentText(String.format(mContext.getString(R.string.loading_photo_percent), currentProgress));
        }
        generatedNotification = notificationBuilder.build();
        return generatedNotification;
    }

    private boolean isVersionOld() {
        return android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
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
                                messages.getAllCount(), messages.getAllCount())
                ));
        for (SerializableToJson item : messages) {
            MessageStack.Message message = (MessageStack.Message) item;
            Spannable spanMessage = new SpannableString(String.format(mContext.getString(R.string.notification_message_format), message.mName, message.mTitle));
            spanMessage.setSpan(new StyleSpan(Typeface.BOLD), 0, message.mName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            inboxStyle.addLine(spanMessage);
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

    public enum Type {PROGRESS, STANDARD, FAIL, ACTIONS}

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