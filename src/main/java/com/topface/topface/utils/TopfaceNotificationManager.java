package com.topface.topface.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.ui.views.ImageViewRemote;

public class TopfaceNotificationManager {
    private static TopfaceNotificationManager mInstance;
    public static final int NOTIFICATION_ID = 1312; //Completely random number


    private NotificationManager notificationManager;
    private Context ctx;

    private static int lastId = 1314;

    public static TopfaceNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TopfaceNotificationManager(context);
        }
        return mInstance;
    }

    private TopfaceNotificationManager(Context context) {

        ctx = context;
        notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /*
        isTextNotification - разворачивать нотификацию как текст - true, как картинку - false
     */
    public int showNotification(String title, String message, boolean isTextNotification, Bitmap icon, int unread, Intent intent, boolean doNeedReplace) {


        int id = NOTIFICATION_ID;
        if (doNeedReplace) {
            id = ++lastId;
        }

        TopfaceNotification notification = new TopfaceNotification(ctx);
        notification.setType(TopfaceNotification.Type.STANDARD);
        notification.setImage(icon);
        notification.setTitle(title);
        notification.setText(message);
        notification.setIsTextNotification(isTextNotification);
        notification.setUnread(unread);
        notification.setId(id);

        notificationManager.notify(id, notification.generate(intent));
        return id;
    }

    public int showProgressNotification(String title, Bitmap icon, Intent intent) {
        int id = ++lastId;
        try {
            TopfaceNotification not = new TopfaceNotification(ctx);
            not.setType(TopfaceNotification.Type.PROGRESS);
            not.setImage(icon);
            not.setTitle(title);
            not.setId(id);
            notificationManager.notify(id, not.generate(intent));
        } catch (Exception e) {
            Debug.error(e);
        }
        return id;
    }

    public int showFailNotification(String title, String msg, Bitmap icon, Intent intent) {
        int id = ++lastId;
        try {
            TopfaceNotification not = new TopfaceNotification(ctx);
            not.setType(TopfaceNotification.Type.FAIL);
            not.setTitle(title);
            not.setText(msg);
            not.setImage(icon);
            not.setId(id);
            //noinspection deprecation
            notificationManager.notify(id, not.generate(intent));
        } catch (Exception e) {
            Debug.error(e);
        }
        return id;
    }

    public void cancelNotification(int id) {

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

    public static class TopfaceNotification {
        private Bitmap image;
        private String text;
        private String title;

        private int id;

        public enum Type {PROGRESS, STANDARD, FAIL}

        private Type type;
        private boolean isTextNotification;

        private int unread = 0;
        private Context context;
        private float width = 64;
        private float height = 64;
        private NotificationCompat.Builder notificationBuilder;

        public TopfaceNotification(Context context) {
            this.context = context;
            float scale = context.getResources().getDisplayMetrics().density;
            width *= scale;
            height *= scale;
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

        public Notification generate(Intent intent) {
            notificationBuilder = new NotificationCompat.Builder(context);
            switch (type) {
                case PROGRESS:
                    return generateProgress(isVersionOld(), intent);
                case FAIL:
                    return generateFail(isVersionOld(), intent);
                case STANDARD:
                    return generateStandard(intent);
            }
            return null;
        }

        private boolean isVersionOld() {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        }

        @SuppressWarnings("UnusedDeclaration")
        private Notification generateStandard(Intent intent) {
            notificationBuilder.setSmallIcon(R.drawable.ic_notification);

            if (image != null) {
                Bitmap scaledIcon = Utils.clippingBitmap(image);
                if (scaledIcon != null) {
                    notificationBuilder.setLargeIcon(scaledIcon);
                }
            } else {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_notification));
            }
            if (Settings.getInstance().isVibrationEnabled()) {
                notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

            }

            notificationBuilder.setSound(Settings.getInstance().getRingtone());
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

        private Notification generateSuccess(boolean isOld, Intent intent) {
            try {
                notificationBuilder.setSmallIcon(R.drawable.ic_notification);

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fail_notification_layout);
                if (image != null) {
                    Bitmap scaledIcon = Utils.clippingBitmap(image);
                    if (scaledIcon != null) {
                        views.setBitmap(R.id.fnAvatar, "setImageBitmap", image);
                    }
                }

                views.setTextViewText(R.id.fnTitle, title);
                views.setTextViewText(R.id.fnMsg, text);
                views.setViewVisibility(R.id.fnRetry, View.GONE);

                if (!isOld) {
                    if (!isTextNotification) {
                        generateBigPicture();
                    } else {
                        generateBigText();
                    }
                }
                notificationBuilder.setSound(Settings.getInstance().getRingtone());
                if (Settings.getInstance().isVibrationEnabled()) {
                    notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

                }
                PendingIntent resultPendingIntent = generatePendingIntent(intent);
                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setContentIntent(resultPendingIntent);
                Notification not = notificationBuilder.build();

                not.contentView = views;
                return not;
            } catch (Exception e) {
                Debug.error(e);
            }
            return null;
        }

        private Notification generateFail(boolean isOld, Intent intent) {
            try {
                notificationBuilder.setSmallIcon(R.drawable.ic_notification);

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fail_notification_layout);
                if (image != null) {
                    Bitmap scaledIcon = Utils.clippingBitmap(image);
                    if (scaledIcon != null) {
                        views.setBitmap(R.id.fnAvatar, "setImageBitmap", image);
                    }
                }

                views.setTextViewText(R.id.fnTitle, title);
                views.setTextViewText(R.id.fnMsg, text);
                if (Settings.getInstance().isVibrationEnabled()) {
                    notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

                }
                if (!isOld) {

                    Intent retryIntent = new Intent(AddPhotoHelper.CANCEL_NOTIFICATION_RECEIVER + intent.getParcelableExtra("PhotoUrl"));
                    retryIntent.putExtra("id", id);
                    retryIntent.putExtra("isRetry", true);

                    views.setOnClickPendingIntent(R.id.fnRetry, generateBigPicture(retryIntent, context.getString(R.string.general_dialog_retry)));
                } else {
                    views.setViewVisibility(R.id.fnRetry, View.GONE);
                }
                PendingIntent resultPendingIntent = generatePendingIntent(intent);
//                notificationBuilder.setAutoCancel(true);
                notificationBuilder.setContentIntent(resultPendingIntent);
                Notification not = notificationBuilder.build();

                not.contentView = views;
                return not;
            } catch (Exception e) {
                Debug.error(e);
            }
            return null;
        }

        private Notification generateProgress(boolean isOld, Intent intent) {
            try {
                notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_upload);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notifications_progress_layout);
                if (image != null) {
                    Bitmap scaledIcon = Utils.clippingBitmap(image);
                    if (scaledIcon != null) {
                        views.setBitmap(R.id.notificationImage, "setImageBitmap", image);
                    }
                }

                views.setTextViewText(R.id.nfTitle, title);
                if (!isOld) {
                    Intent cancelIntent = new Intent(AddPhotoHelper.CANCEL_NOTIFICATION_RECEIVER + intent.getParcelableExtra("PhotoUrl"));
                    cancelIntent.putExtra("id", id);
                    views.setOnClickPendingIntent(R.id.notCancelButton, generateBigPicture(cancelIntent, context.getString(R.string.general_cancel)));
                } else {
                    views.setViewVisibility(R.id.notCancelButton, View.GONE);
                }
                PendingIntent resultPendingIntent = generatePendingIntent(intent);

                notificationBuilder.setContentIntent(resultPendingIntent);
                Notification not = notificationBuilder.build();
                not.contentView = views;
                //noinspection deprecation
                return not;
            } catch (Exception e) {
                Debug.error(e);
            }
            return null;
        }

        private void generateBigPicture() {
            Bitmap tficon = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_notification);
            NotificationCompat.BigPictureStyle inboxStyle =
                    new NotificationCompat.BigPictureStyle(notificationBuilder.setContentTitle(title));

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
                    new NotificationCompat.BigTextStyle(notificationBuilder.setLargeIcon(image).setContentTitle(title));

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
    }

}
