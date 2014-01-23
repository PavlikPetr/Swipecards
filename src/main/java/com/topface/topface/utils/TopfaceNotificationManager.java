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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.topface.topface.R;
import com.topface.topface.imageloader.DefaultImageLoader;
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
    public int  showNotification(String title, String message, boolean isTextNotification,
                                Bitmap icon, int unread, Intent intent, boolean doNeedReplace) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, false, TopfaceNotification.Type.STANDARD);
    }

    public int  showNotification(String title, String message, boolean isTextNotification,
                                 Bitmap icon, int unread, Intent intent, boolean doNeedReplace, TopfaceNotification.Type type) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, false, type);
    }

    public void showNotification(final String title, final String message, final boolean isTextNotification,
                                String uri, final int unread, final Intent intent, final boolean doNeedReplace,
                                final NotificationImageListener listener) {
        DefaultImageLoader.getInstance().getImageLoader().loadImage(uri, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (listener != null) {
                    listener.onFail();
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (listener != null && listener.needShowNotification()) {
                    listener.onSuccess( showNotification(title, message, isTextNotification, loadedImage, unread, intent, doNeedReplace));
                } else {
                    showNotification(title, message, isTextNotification, loadedImage, unread, intent, doNeedReplace);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (listener != null) {
                    listener.onFail();
                }
            }
        });
    }

    public int showNotification(String title, String message, boolean isTextNotification,
                                Bitmap icon, int unread, Intent intent, boolean doNeedReplace,
                                boolean ongoing, TopfaceNotification.Type type) {
        int id = NOTIFICATION_ID;
        if (doNeedReplace) {
            id = newNotificationId();
        }
        TopfaceNotification notification = new TopfaceNotification(ctx);
        notification.setType(type);
        notification.setImage(icon);
        notification.setTitle(title);
        notification.setText(message);
        notification.setIsTextNotification(isTextNotification);
        notification.setUnread(unread);
        notification.setId(id);
        notification.setOngoing(ongoing);

        notificationManager.notify(id, notification.generate(intent));
        return id;
    }

    /**
     * Notification id which won't be conflicting with previous ids
     *
     * @return notification id
     */
    public int newNotificationId() {
        return ++lastId;
    }

    public int showNotificationWithActions(int id, String title, String message, Bitmap icon,
                                           boolean ongoing,
                                           NotificationAction[] actions) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setContentTitle(title)
                .setContentText(message)
                .setOngoing(ongoing);
        setNotificationIcon(ctx, builder, icon);
        for (NotificationAction action : actions) {
            builder.addAction(action.iconResId, action.text, action.intent);
        }
        notificationManager.notify(id, builder.build());
        return id;
    }

    public void showProgressNotification(final String title, String uri, final Intent intent, final NotificationImageListener listener) {
        DefaultImageLoader.getInstance().getImageLoader().loadImage(uri, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (listener != null) {
                    listener.onFail();
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (listener != null) {
                    if (listener.needShowNotification()) {
                        listener.onSuccess(showProgressNotification(title, loadedImage, intent));
                    }
                } else {
                    showProgressNotification(title, loadedImage, intent);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (listener != null) {
                    listener.onFail();
                }
            }
        });
    }

    public int showProgressNotification(String title, Bitmap icon, Intent intent) {
        return showNotification(title, null, false, icon, 0, intent, false, TopfaceNotification.Type.PROGRESS);
    }

    public int showFailNotification(String title, String msg, Bitmap icon, Intent intent) {
        return showNotification(title, msg, false, icon, 0, intent, false, TopfaceNotification.Type.FAIL);
    }

    public void showFailNotification(final String title, final String msg, final String iconUri, final Intent intent, final NotificationImageListener listener) {
        DefaultImageLoader.getInstance().getImageLoader().loadImage(iconUri, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (listener != null) {
                    listener.onFail();
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (listener != null) {
                    if(listener.needShowNotification()) {
                        listener.onSuccess(showFailNotification(title, msg, loadedImage, intent));
                    }
                } else {
                    showFailNotification(title, msg, loadedImage, intent);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (listener != null) {
                    listener.onFail();
                }
            }
        });
    }

    public void cancelNotification(int id) {
        notificationManager.cancel(id);
    }

    private static void setNotificationIcon(Context context, NotificationCompat.Builder builder, Bitmap image) {
        builder.setSmallIcon(R.drawable.ic_notification);

        if (image != null) {
            Bitmap scaledIcon = Utils.clipBitmap(image);
            if (scaledIcon != null) {
                builder.setLargeIcon(scaledIcon);
            }
        } else {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_notification));
        }
    }

    public static class TopfaceNotification {
        private Bitmap image;
        private String text;
        private String title;

        private int id;
        private boolean ongoing;

        public enum Type {PROGRESS, STANDARD, FAIL}

        private Type type;
        private boolean isTextNotification;

        private int unread = 0;
        private Context context;
        private NotificationCompat.Builder notificationBuilder;

        public TopfaceNotification(Context context) {
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

        public Notification generate(Intent intent) {
            notificationBuilder = new NotificationCompat.Builder(context);
            notificationBuilder.setOngoing(ongoing);
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
            setNotificationIcon(context, notificationBuilder, image);
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

        private Notification generateFail(boolean isOld, Intent intent) {
            try {
                notificationBuilder.setSmallIcon(R.drawable.ic_notification);

                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.fail_notification_layout);
                if (image != null) {
                    Bitmap scaledIcon = Utils.clipBitmap(image);
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
                    Bitmap scaledIcon = Utils.clipBitmap(image);
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

    public static interface NotificationImageListener {
        public void onSuccess(int id);
        public void onFail();
        //Нужно, если в каких-то случаях, после асинхронной загрузки фото,
        //нотификацию показывать уже не надо. Если в любом случае надо, можно
        //передать этот listener null или поставить у этого метода return true;
        public boolean needShowNotification();
    }
}
