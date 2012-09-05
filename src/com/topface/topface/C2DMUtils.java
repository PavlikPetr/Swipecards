package com.topface.topface;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.google.android.c2dm.C2DMessaging;
import com.topface.topface.R;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.MainActivity;

public class C2DMUtils {
    // Data
    public static final int C2DM_NOTIFICATION_ID = 1001;
    public static final String C2DM_REGISTERED = "c2dmRegistered";
    public static final String C2DM_NOTIFICATION = "com.topface.topface.action.NOTIFICATION";

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

            notification.defaults |= Notification.DEFAULT_SOUND;
            //notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            //HELLO_ID;

            int uid = Integer.parseInt(extra.getStringExtra("id"));
            int notificationId = C2DM_NOTIFICATION_ID;
            Intent i;
            if (uid > 0) {
                i = new Intent(context, ChatActivity.class);
                i.putExtra(ChatActivity.INTENT_USER_ID, uid);
                i.putExtra(ChatActivity.INTENT_USER_NAME, extra.getStringExtra("name"));
            }
            else {
                i = new Intent(context, MainActivity.class);
            }
            i.putExtra("C2DM", true);

            //Активити, котолрое будет запущено после уведомления
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_SINGLE_TOP);

            //Обновляем (или устанавливаем) то, что показывается в панели уведомлений
            notification.setLatestEventInfo(context, context.getString(R.string.app_name), data, contentIntent);

            mNotificationManager.notify(notificationId, notification);
        }
    }
    //---------------------------------------------------------------------------
}
