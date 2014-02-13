package com.topface.topface.utils.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.topface.topface.imageloader.DefaultImageLoader;

public class UserNotificationManager {
    private static UserNotificationManager mInstance;
    public static final int NOTIFICATION_ID = 1312; //Completely random number

    private NotificationManager mNotificationManager;
    private Context mContext;

    private static int lastId = 1314;

    public static UserNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UserNotificationManager(context);
        }
        return mInstance;
    }

    private UserNotificationManager(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /*
        isTextNotification - разворачивать нотификацию как текст - true, как картинку - false
     */
    public int showNotification(String title, String message, boolean isTextNotification,
                                Bitmap icon, int unread, Intent intent, boolean doNeedReplace) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, false, UserNotification.Type.STANDARD, null);
    }

    private int showNotification(String title, String message, boolean isTextNotification,
                                 Bitmap icon, int unread, Intent intent, boolean doNeedReplace, UserNotification.Type type) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, false, type, null);
    }

    private int showNotification(String title, String message, boolean isTextNotification,
                                 Bitmap icon, int unread, Intent intent, boolean doNeedReplace, boolean ongoing, UserNotification.Type type) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, ongoing, type, null);
    }

    public void showNotificationAsync(final String title, final String message, final boolean isTextNotification,
                                      String uri, final int unread, final Intent intent, final boolean doNeedReplace) {
        showNotificationAsync(title, message, isTextNotification, uri, unread, intent, doNeedReplace, null);
    }

    public void showNotificationAsync(final String title, final String message, final boolean isTextNotification,
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
                    listener.onSuccess(showNotification(title, message, isTextNotification, loadedImage, unread, intent, doNeedReplace));
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

    public int showNotificationWithActions(String title, String message, Bitmap icon,
                                           boolean ongoing,
                                           UserNotification.NotificationAction[] actions) {
        return showNotification(title, message, false, icon, 0, null, true, ongoing, UserNotification.Type.ACTIONS, actions);
    }

    public int showProgressNotification(String title, Bitmap icon, Intent intent) {
        return showNotification(title, null, false, icon, 0, intent, false, false, UserNotification.Type.PROGRESS);
    }

    public void showProgressNotificationAsync(final String title, String uri, final Intent intent, final NotificationImageListener listener) {
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


    public int showFailNotification(String title, String msg, Bitmap icon, Intent intent) {
        return showNotification(title, msg, false, icon, 0, intent, true, UserNotification.Type.FAIL);
    }

    public void showFailNotificationAsync(final String title, final String msg, final String iconUri, final Intent intent, final NotificationImageListener listener) {
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
                    if (listener.needShowNotification()) {
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

    private int showNotification(String title, String message, boolean isTextNotification,
                                 Bitmap icon, int unread, Intent intent, boolean createNew,
                                 boolean ongoing, UserNotification.Type type, UserNotification.NotificationAction[] actions) {
        int id = NOTIFICATION_ID;
        if (createNew) {
            id = newNotificationId();
        }
        UserNotification notification = new UserNotification(mContext);
        notification.setType(type);
        notification.setImage(icon);
        notification.setTitle(title);
        notification.setText(message);
        notification.setIsTextNotification(isTextNotification);
        notification.setUnread(unread);
        notification.setId(id);
        notification.setOngoing(ongoing);

        mNotificationManager.notify(id, notification.generate(intent, actions));
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

    public void cancelNotification(int id) {
        mNotificationManager.cancel(id);
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
