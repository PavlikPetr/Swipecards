package com.topface.topface;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import com.google.android.c2dm.C2DMessaging;
import com.topface.topface.ui.*;
import com.topface.topface.utils.Settings;

import java.util.Timer;
import java.util.TimerTask;

public class C2DMUtils {
    // Data
    public static final int C2DM_NOTIFICATION_ID = 1001;
    public static final String C2DM_REGISTERED = "c2dmRegistered";
    public static final String C2DM_NOTIFICATION = "com.topface.topface.action.NOTIFICATION";

    public static final int C2DM_TYPE_UNKNOWN  = -1;
    public static final int C2DM_TYPE_MESSAGE  = 0;
    public static final int C2DM_TYPE_SYMPATHY = 1;
    public static final int C2DM_TYPE_LIKE     = 2;
    public static final int NOTIFICATION_CANCEL_DELAY = 3000;

    //---------------------------------------------------------------------------
    public static void init(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        Boolean registered = preferences.getBoolean(C2DM_REGISTERED, false);

        //Проверяем, зарегестрированны ли мы (т.е. есть ли у нас токен) и то, что мы удачно отправили его на сервер
        if (!registered || C2DMessaging.getRegistrationId(context).equals("")) {
            //Если это не так, то заново регистрируемся
            C2DMessaging.register(context, C2DMReceiver.SENDER_ID);
        }
    }

    //---------------------------------------------------------------------------
    public static void setRegisteredFlag(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(C2DM_REGISTERED, true);
        editor.commit();
    }

    //---------------------------------------------------------------------------
    public static void showNotification(Intent extra, Context context) {
        String data = extra.getStringExtra("text");
        if (data != null) {

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Set the icon, scrolling text and timestamp
            Notification notification = new Notification(R.drawable.ic_statusbar, data, System.currentTimeMillis());

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            if (Settings.getInstance().isVibrationEnabled()) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }

            notification.sound = Uri.parse(Settings.getInstance().getRingtone());

            //HELLO_ID;
            int notificationId = C2DM_NOTIFICATION_ID;
            Intent i;
            String typeString = extra.getStringExtra("type");
            int type = typeString != null ? Integer.parseInt(typeString) : C2DM_TYPE_UNKNOWN;

            switch (type) {
                case C2DM_TYPE_MESSAGE:
                    i = new Intent(context, ChatActivity.class);
                    i.putExtra(
                            ChatActivity.INTENT_USER_ID,
                            Integer.parseInt(extra.getStringExtra("id"))
                    );
                    i.putExtra(ChatActivity.INTENT_USER_NAME, extra.getStringExtra("name"));
                    i.putExtra(ChatActivity.INTENT_USER_AVATAR, extra.getStringExtra("avatar"));
                    break;

                case C2DM_TYPE_SYMPATHY:
                    i = new Intent(context, SymphatyActivity.class);
                    break;

                case C2DM_TYPE_LIKE:
                    i = new Intent(context, LikesActivity.class);
                    break;

                default:
                    i = new Intent(context, DashboardActivity.class);

            }

            i.putExtra("C2DM", true);

            //Активити, котолрое будет запущено после уведомления
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_SINGLE_TOP);

            //Обновляем (или устанавливаем) то, что показывается в панели уведомлений
            notification.setLatestEventInfo(context, context.getString(R.string.app_name), data, contentIntent);

            mNotificationManager.notify(notificationId, notification);
        }
    }

    public static void cancelNotification(final Context context) {
        //Отменяем уведомления с небольшой задержкой,
        //что бы на ICS успело доиграть уведомление (длинные не успеют. но не страшно. все стандартные - короткие)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(C2DM_NOTIFICATION_ID);
            }
        }, NOTIFICATION_CANCEL_DELAY);

    }
    //---------------------------------------------------------------------------
}
