package com.topface.topface.utils.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.receivers.NotificationClosedReceiver;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.gcmutils.GCMUtils.User;

public class UserNotificationManager {
    public static final int NOTIFICATION_ID = 1312; //Completely random number
    public static final int MESSAGES_ID = 1311;
    public static final int TARGET_IMAGE_SIZE = 256;
    public static final int TARGET_IMAGE_SIZE_PRE_JB = 128;
    private static UserNotificationManager mInstance;
    private static int lastId = 1314;
    private NotificationManagerCompat mNotificationManager;
    private Context mContext;
    private ImageSize mImageSize;

    private UserNotificationManager(Context context) {
        mContext = context;
        mNotificationManager = NotificationManagerCompat.from(mContext);
    }

    public static UserNotificationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UserNotificationManager(context);
        }
        return mInstance;
    }

    public UserNotification showNotification(String title, String message, boolean isTextNotification,
                                             Bitmap icon, int unread, Intent intent, boolean doNeedReplace) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, false, UserNotification.Type.STANDARD, null, null);
    }

    /*
        isTextNotification - разворачивать нотификацию как текст - true, как картинку - false
     */
    public UserNotification showNotification(String title, String message, boolean isTextNotification,
                                             Bitmap icon, int unread, Intent intent, boolean doNeedReplace, User user) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, false, UserNotification.Type.STANDARD, null, user);
    }

    private UserNotification showNotification(String title, String message, boolean isTextNotification,
                                              Bitmap icon, int unread, Intent intent, boolean doNeedReplace, UserNotification.Type type) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, false, type, null, null);
    }

    private UserNotification showNotification(String title, String message, boolean isTextNotification,
                                              Bitmap icon, int unread, Intent intent, boolean doNeedReplace, boolean ongoing, UserNotification.Type type) {
        return showNotification(title, message, isTextNotification, icon, unread, intent,
                doNeedReplace, ongoing, type, null, null);
    }

    public void showNotificationAsync(final String title, final String message, User user, final boolean isTextNotification,
                                      String uri, final int unread, final Intent intent, final boolean doNeedReplace) {
        showNotificationAsync(title, message, isTextNotification, uri, unread, intent, doNeedReplace, null, user);
    }

    public void showNotificationAsync(final String title, final String message, final boolean isTextNotification,
                                      String uri, final int unread, final Intent intent, final boolean doNeedReplace,
                                      final NotificationImageListener listener, final User user) {
        DefaultImageLoader.getInstance(mContext).getImageLoader().loadImage(uri, getTargetImageSize(), new ImageLoadingListener() {
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
                    listener.onSuccess(showNotification(title, message, isTextNotification, loadedImage, unread, intent, doNeedReplace, user));
                } else {
                    showNotification(title, message, isTextNotification, loadedImage, unread, intent, doNeedReplace, user);
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

    public UserNotification showNotificationWithActions(String title, String message, Bitmap icon,
                                                        boolean ongoing,
                                                        UserNotification.NotificationAction[] actions,
                                                        int notificationId) {
        return showNotification(title, message, false, icon, 0, null, notificationId, ongoing, UserNotification.Type.ACTIONS, actions, null);
    }

    public UserNotification showProgressNotification(String title, Bitmap icon, Intent intent) {
        return showNotification(title, null, false, icon, 0, intent, true, false, UserNotification.Type.PROGRESS);
    }

    public void showProgressNotificationAsync(final String title, String uri, final Intent intent, final NotificationImageListener listener) {
        DefaultImageLoader.getInstance(mContext).getImageLoader().loadImage(uri, getTargetImageSize(), new ImageLoadingListener() {
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

    private ImageSize getTargetImageSize() {
        //среднее отбалдическое разрешение, сделано что бы показывать большие изображения в уведомлениях
        if (mImageSize == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mImageSize = new ImageSize(TARGET_IMAGE_SIZE, TARGET_IMAGE_SIZE);
            } else {
                mImageSize = new ImageSize(TARGET_IMAGE_SIZE_PRE_JB, TARGET_IMAGE_SIZE_PRE_JB);
            }
        }

        return mImageSize;
    }


    public UserNotification showFailNotification(String title, String msg, Bitmap icon, Intent intent) {
        return showNotification(title, msg, false, icon, 0, intent, true, UserNotification.Type.FAIL);
    }

    public void showFailNotificationAsync(final String title, final String msg, final String iconUri, final Intent intent, final NotificationImageListener listener) {
        DefaultImageLoader.getInstance(mContext).getImageLoader().loadImage(iconUri, getTargetImageSize(), new ImageLoadingListener() {
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

    private UserNotification showNotification(String title, String message, boolean isTextNotification,
                                              Bitmap icon, int unread, Intent intent, boolean createNew,
                                              boolean ongoing, UserNotification.Type type, UserNotification.NotificationAction[] actions,
                                              User user) {
        return showNotification(
                title,
                message,
                isTextNotification,
                icon,
                unread,
                intent,
                createNew ? newNotificationId() : NOTIFICATION_ID,
                ongoing,
                type,
                actions,
                user
        );
    }

    private UserNotification showNotification(String title, String message, boolean isTextNotification,
                                              Bitmap icon, int unread, Intent intent, int id,
                                              boolean ongoing, UserNotification.Type type, UserNotification.NotificationAction[] actions,
                                              User user) {
        UserNotification notification = new UserNotification(mContext);
        MessageStack messagesStack = new MessageStack();
        if (intent != null && intent.getIntExtra(Static.INTENT_REQUEST_KEY, -1) == ChatActivity.INTENT_CHAT) {
            id = MESSAGES_ID;
            messagesStack = saveMessageStack(message, user);
            notification.setWearReply(mContext, intent);
        }
        notification.setType(type);
        notification.setImage(icon);
        notification.setTitle(title);
        notification.setText(message);
        notification.setIsTextNotification(isTextNotification);
        notification.setUnread(unread);
        notification.setId(id);
        notification.setOngoing(ongoing);
        notification.setMessages(messagesStack);
        notification.setIntent(intent);
        /*
        onDeleteIntent triggers when notification is deleted by user. We need it to gather
        statistics about deleted notifications.
         */
        if (intent != null) {
            Intent onDeleteIntent = new Intent(NotificationClosedReceiver.NOTIFICATION_CLOSED);
            onDeleteIntent.putExtra(GCMUtils.GCM_TYPE, intent.getIntExtra(GCMUtils.GCM_TYPE, -1));
            onDeleteIntent.putExtra(GCMUtils.GCM_LABEL, intent.getStringExtra(GCMUtils.GCM_LABEL));
            notification.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, onDeleteIntent, 0));
        }
        try {
            mNotificationManager.notify(id, notification.generate(actions));
        } catch (Exception e) {
            Debug.error(e);
        }
        return notification;
    }

    public void showBuildedNotification(UserNotification notification) {
        mNotificationManager.notify(notification.getId(), notification.getGeneratedNotification());
    }

    public void showSimpleNotification(Notification notification) {
        mNotificationManager.notify(1, notification);
    }

    private MessageStack saveMessageStack(String message, User user) {
        UserConfig config = App.getUserConfig();
        MessageStack messagesStack = config.getNotificationMessagesStack();
        if (user != null) {
            messagesStack.addFirst(new MessageStack.Message(user.name, message));
            config.setNotificationMessagesStack(messagesStack);
            config.saveConfig();
        }
        return messagesStack;
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
        if (id == MESSAGES_ID) {
            App.getUserConfig().resetNotificationMessagesStack();
        }
        try {
            mNotificationManager.cancel(id);
        } catch (IllegalStateException e) {
            /*
            In some cases NotificationManager may throw IllegalStateException on notification cancel
             */
            Debug.error(e);
        }
    }

    public static interface NotificationImageListener {
        public void onSuccess(UserNotification notification);

        public void onFail();

        //Нужно, если в каких-то случаях, после асинхронной загрузки фото,
        //нотификацию показывать уже не надо. Если в любом случае надо, можно
        //передать этот listener null или поставить у этого метода return true;
        public boolean needShowNotification();
    }
}
