package com.topface.topface;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;
import com.topface.framework.utils.Debug;
import com.topface.topface.utils.Editor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class GCMIntentService extends GCMBaseIntentService {
    public static final String SENDER_ID = "932206034265";
    public static AtomicBoolean isOnMessageReceived = new AtomicBoolean(false);

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
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


    @Override
    protected void onError(Context context, String s) {
        Debug.error(String.format("GCM: Error: %s", s));
    }

    @Override
    protected void onRegistered(final Context context, final String registrationId) {
        Debug.log("GCM: Registered: " + registrationId);
        GCMUtils.sendRegId(context, registrationId);
    }


    @Override
    protected void onUnregistered(Context context, String s) {
        Debug.log(String.format("GCM: onUnregistered: %s", s));
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
}
