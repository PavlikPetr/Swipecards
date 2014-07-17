package com.topface.topface;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.topface.framework.utils.Debug;
import com.topface.statistics.NotificationStatistics;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.gcmutils.GcmBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class GcmIntentService extends IntentService {
    public static final String SENDER_ID = "932206034265";
    public static AtomicBoolean isOnMessageReceived = new AtomicBoolean(false);

    public GcmIntentService() {
        super(SENDER_ID);
    }

    protected void onMessage(final Context context, final Intent intent) {
        Debug.log("GCM: onMessage");
        isOnMessageReceived.set(true);
        if (intent != null) {
            Debug.log("GCM: Try show\n" + intent.getExtras());
            if (GCMUtils.showNotificationIfNeed(intent, context)) {
                //Сообщаем о том что есть новое уведомление и нужно обновить список
                Intent broadcastReceiver = new Intent(GCMUtils.GCM_NOTIFICATION);
                String user = intent.getStringExtra("user");

                if (user != null) {
                    String userId = getUserId(user);
                    broadcastReceiver.putExtra(GCMUtils.USER_ID_EXTRA, userId);
                    context.sendBroadcast(broadcastReceiver);
                    Intent updateIntent = null;
                    int type = GCMUtils.getType(intent);
                    NotificationStatistics.sendReceived(type, GCMUtils.getLabel(intent));
                    switch (GCMUtils.getType(intent)) {
                        case GCMUtils.GCM_TYPE_MESSAGE:
                        case GCMUtils.GCM_TYPE_DIALOGS:
                        case GCMUtils.GCM_TYPE_GIFT:
                            updateIntent = new Intent(GCMUtils.GCM_DIALOGS_UPDATE);
                            break;
                        case GCMUtils.GCM_TYPE_MUTUAL:
                            updateIntent = new Intent(GCMUtils.GCM_MUTUAL_UPDATE);
                            break;
                        case GCMUtils.GCM_TYPE_LIKE:
                            updateIntent = new Intent(GCMUtils.GCM_LIKE_UPDATE);
                            break;
                        case GCMUtils.GCM_TYPE_GUESTS:
                            updateIntent = new Intent(GCMUtils.GCM_GUESTS_UPDATE);
                            break;
                        case GCMUtils.GCM_TYPE_PEOPLE_NEARBY:
                            updateIntent = new Intent(GCMUtils.GCM_PEOPLE_NEARBY_UPDATE);
                            break;
                    }
                    if (updateIntent != null) {
                        context.sendBroadcast(updateIntent);
                    }
                }
                if (Editor.isEditor()) {
                    Intent test = new Intent("com.topface.testapp.GCMTest");
                    test.putExtras(intent.getExtras());
                    App.getContext().sendBroadcast(test);
                }
            }

        }
    }



    private String getUserId(String user) {
        String id = "";
        try {
            JSONObject userJSON = new JSONObject(user);
            id = userJSON.optString("id");
        } catch (JSONException e) {
            Debug.error(e);
        }

        return id;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                onMessage(App.getContext(), intent);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}
