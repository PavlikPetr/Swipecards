package com.topface.topface;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import com.google.android.gcm.GCMBaseIntentService;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import org.json.JSONException;
import org.json.JSONObject;

public class GCMIntentService extends GCMBaseIntentService {
    public static final String SENDER_ID = "932206034265";


    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onMessage(final Context context, final Intent intent) {
        Debug.log("onMessage");
        if (intent != null) {
            if (Settings.getInstance().isNotificationEnabled()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        GCMUtils.showNotification(intent, context);
                        Looper.loop();
                    }

                }).start();
            }
            //Сообщаем о том что есть новое уведомление и нужно обновить список
            Intent broadcastReceiver = new Intent(GCMUtils.GCM_NOTIFICATION);
            String user = intent.getStringExtra("user");
            if (user != null) {
                broadcastReceiver.putExtra("id", getUserId(user));
                context.sendBroadcast(broadcastReceiver);
            }
        }
    }

    @Override
    protected void onError(Context context, String s) {
        Debug.error(String.format("GCM Error: %s", s));
    }

    @Override
    protected void onRegistered(final Context context, final String registrationId) {
        GCMUtils.sendRegId(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String s) {
        Debug.log(String.format("GCM onUnregistered: %s", s));
    }

    private String getUserId(String user) {
        String id = "";
        try {
            JSONObject userJSON = new JSONObject(user);
            id = userJSON.optString("id");
        } catch (JSONException e) {
            Debug.error(e);
        } catch (Exception e) {
            Debug.error(e);
        }

        return id;
    }
}
