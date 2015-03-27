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
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.topface.framework.imageloader.BitmapUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.SerializableToJson;
import com.topface.topface.data.experiments.MessagesWithTabs;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.gcmutils.GCMUtils;

public class UserNotification {

    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    // sometimes with code 0 and 1 (ones that are inspected) wear does not send intent to app with error:
    // removeAppFromTaskLocked: token=AppWindowToken{adee2558 token=Token{adf4b7d0 ActivityRecord{adee3d40 u0 com.google.android.wearable.app/com.google.android.clockwork.home.RemoteActionConfirmationActivity t1 f}}} not found.
    private static final int WEAR_REPLY_REQUEST_CODE = 593;
    public static final int ICON_SIZE = 64;
    private static final int ONE_MESSAGE = 1;
    private static final int FEW_MESSAGES = 2;
    public static final String NOTIFICATION_ID = "notification_id";
    private Bitmap mImage;
    private String mText;
    private String mTitle;


    private Intent mIntent;
    private PendingIntent mDeleteIntent;
    private int mId;
    private boolean mOngoing;


    Notification generatedNotification;

    private MessageStack messages;
    private Type mType;
    private boolean mIsTextNotification;
    private int unread = 0;
    private Context mContext;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationCompat.WearableExtender mWearableExtender;
    private int mGCMType;

    public UserNotification(Context context) {
        this.mContext = context;
    }

    public static int getIconSize(Context context) {
        return (int) (context.getResources().getDisplayMetrics().density * ICON_SIZE);
    }

    public void setType(Type mType) {
        this.mType = mType;
    }

    public void setGCMType(int type) {
        this.mGCMType = type;
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

    public void setDeleteIntent(PendingIntent intent) {
        mDeleteIntent = intent;
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

            int notification = 0;
            if (App.getUserConfig().isVibrationEnabled()) {
                notification |= android.app.Notification.DEFAULT_VIBRATE;
            }
            if (App.getUserConfig().isLEDEnabled()) {
                notification |= Notification.DEFAULT_LIGHTS;
            }
            notificationBuilder.setDefaults(notification);
            notificationBuilder.setSound(App.getUserConfig().getRingtone());
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
        notificationBuilder.setContentTitle(mTitle);
        notificationBuilder.setContentText(mText);
        if (mIsTextNotification) {
            setIcons();
        } else {
            generateBigPicture();
        }
        if (unread > 0) {
            notificationBuilder.setNumber(unread);
        }
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setContentIntent(getPendingIntent(mIntent));

        if (mDeleteIntent != null) {
            notificationBuilder.setDeleteIntent(mDeleteIntent);
        }
        if (mWearableExtender != null) {
            notificationBuilder.extend(mWearableExtender);
        }
        generatedNotification = notificationBuilder.build();
        return generatedNotification;
    }

    private android.app.Notification generateFail() {
        addSmallIcon(0);
        notificationBuilder.setContentTitle(mTitle);
        notificationBuilder.setContentText(mText);
        notificationBuilder.setAutoCancel(true);
        setLargeIcon();
        Intent retryIntent = new Intent(AddPhotoHelper.CANCEL_NOTIFICATION_RECEIVER + mIntent.getParcelableExtra("PhotoUrl"));
        retryIntent.putExtra("id", mId);
        retryIntent.putExtra("isRetry", true);
        notificationBuilder.setContentIntent(getPendingIntent(mIntent));
        generatedNotification = notificationBuilder.build();
        return generatedNotification;
    }

    private android.app.Notification generateProgress() {
        notificationBuilder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
        addSmallIcon(android.R.drawable.stat_sys_upload);
        notificationBuilder.setVibrate(new long[]{});
        notificationBuilder.setSound(Uri.EMPTY);
        setLargeIcon();
        notificationBuilder.setContentTitle(mTitle);
        generateBigPicture();
        notificationBuilder.setContentIntent(getPendingIntent(mIntent));
        notificationBuilder.setProgress(100, 100, true);
        if (isVersionOld()) {
            notificationBuilder.setContentText(mContext.getString(R.string.waiting_for_load));
        }
        generatedNotification = notificationBuilder.build();
        return generatedNotification;
    }

    private PendingIntent getPendingIntent(Intent intent) {
        // known issue with pendingIntents on KitKat after uninstall/install
        // https://code.google.com/p/android/issues/detail?id=61850
        if (Build.VERSION.SDK_INT == 19) {
            generatePendingIntent(intent).cancel();
        }
        return generatePendingIntent(intent);
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
        addSmallIcon(0);
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
        Drawable blankDrawable = mContext.getResources().getDrawable(getDefaultSmallIcon());
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
        return generatePendingIntent(intent, 0);
    }

    private PendingIntent generatePendingIntent(Intent intent, int requestCode) {
        PendingIntent resultPendingIntent;
        intent.putExtra(NOTIFICATION_ID, mId);
        if (!TextUtils.equals(intent.getComponent().getClassName(), NavigationActivity.class.toString())) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            // Puts intent with it's parent activities in back stack
            stackBuilder.addNextIntentWithParentStack(intent);
            // Put extra for NavigationActivity to open parent page of intents's component
            Intent parentIntent = stackBuilder.editIntentAt(0);
            putTopLevelFragment(parentIntent, intent);
            parentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            // Gets a PendingIntent containing the entire back stack
            resultPendingIntent = stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return resultPendingIntent;
    }

    private void putTopLevelFragment(Intent parentIntent, Intent targetIntent) {
        String componentName = targetIntent.getComponent().getClassName();
        if (TextUtils.equals(componentName, ChatActivity.class.getCanonicalName())) {
            MessagesWithTabs.equipNavigationActivityIntent(parentIntent);
        }
    }

    private void setDefaultIcon() {
        if (notificationBuilder != null) {
            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                    R.drawable.ic_tf_notification));
        }
    }

    private void setLargeIcon() {
        if (mImage != null) {
            Bitmap scaledIcon = BitmapUtils.getRoundBitmap(BitmapUtils.clipAndScaleBitmap(mImage, getIconSize(mContext), getIconSize(mContext)), 1f);
            if (scaledIcon != null) {
                notificationBuilder.setLargeIcon(scaledIcon);
            } else {
                setDefaultIcon();
            }
        } else {
            setDefaultIcon();
        }
    }

    public void setWearReply(Context context, Intent replyIntent) {
        String replyLabel = context.getResources().getString(R.string.reply);
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel(replyLabel)
                .build();
        PendingIntent replyPendingIntent = generatePendingIntent(replyIntent, WEAR_REPLY_REQUEST_CODE);
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_reply_icon,
                        context.getString(R.string.reply), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
        mWearableExtender = new NotificationCompat.WearableExtender().addAction(action);

    }

    public static String getRemoteInputMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(UserNotification.EXTRA_VOICE_REPLY).toString();
        }
        return null;
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

    private void addSmallIcon(int imageId) {
        if (notificationBuilder != null) {
            notificationBuilder.setSmallIcon(imageId == 0 ? getDefaultSmallIcon() : imageId);
        }
    }

    private int getDefaultSmallIcon() {
        return R.drawable.ic_stat_notify;
    }


    private void setIcons() {
        switch (mGCMType) {
            case GCMUtils.GCM_TYPE_MESSAGE:
                setIcons(ONE_MESSAGE, true, true);
                break;
            case GCMUtils.GCM_TYPE_DIALOGS:
                setIcons(ONE_MESSAGE, false, true);
                break;
            case GCMUtils.GCM_TYPE_GIFT:
                setIcons(ONE_MESSAGE, true, true);
                break;
            case GCMUtils.GCM_TYPE_MUTUAL:
                setIcons(ONE_MESSAGE, false, true);
                break;
            case GCMUtils.GCM_TYPE_LIKE:
                setIcons(ONE_MESSAGE, false, true);
                break;
            case GCMUtils.GCM_TYPE_GUESTS:
                setIcons(ONE_MESSAGE, false, true);
                break;
            case GCMUtils.GCM_TYPE_PEOPLE_NEARBY:
                setIcons(ONE_MESSAGE, false, true);
                break;
            default:
                setIcons(FEW_MESSAGES, false, false, SMALL_ICON_COLOR.BLUE);
                break;
        }
    }

    private void setSmallIconColor(int color) {
        if (notificationBuilder != null) {
            notificationBuilder.setColor(color);
        }
    }

    private void setBlueSmallIcon() {
        setSmallIconColor(App.getContext().getResources().getColor(R.color.light_theme_color_primary));
    }

    private void setPinkSmallIcon() {
        setSmallIconColor(App.getContext().getResources().getColor(R.color.light_theme_color_accent));
    }

    private enum SMALL_ICON_COLOR {
        PINK, BLUE
    }

    private void setIcons(int messagesCount, boolean isStackable, boolean isShowingLowerIcon) {
        setIcons(messagesCount, isStackable, isShowingLowerIcon, SMALL_ICON_COLOR.PINK);
    }

    private void setIcons(int messagesCount, boolean isStackable, boolean isShowingLowerIcon, SMALL_ICON_COLOR smallIconColor) {
        if (notificationBuilder != null) {
            if (messagesCount <= ONE_MESSAGE) {
                if (isShowingLowerIcon) {
                    setLargeIcon();
                }
            } else {
                if (isShowingLowerIcon) {
                    setDefaultIcon();
                }
            }
            addSmallIcon(0);
            if (isStackable) {
                generateInbox();
            } else {
                generateBigText();
            }
            setSmallIconColor(smallIconColor);
        }
    }

    private void setSmallIconColor(SMALL_ICON_COLOR smallIconColor) {
        switch (smallIconColor) {
            case BLUE:
                setBlueSmallIcon();
                break;
            case PINK:
                setPinkSmallIcon();
                break;
        }
    }
}
