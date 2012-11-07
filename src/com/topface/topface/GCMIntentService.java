package com.topface.topface;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import com.google.android.gcm.GCMBaseIntentService;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.RegistrationTokenRequest;
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
        Debug.logJson("GCM","User", user);
        broadcastReceiver.putExtra("id", getUserId(user));
        context.sendBroadcast(broadcastReceiver);
    }

    @Override
    protected void onError(Context context, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRegistered(final Context context, final String registrationId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                Debug.log("onRegistered", registrationId);

                RegistrationTokenRequest registrationRequest = new RegistrationTokenRequest(getApplicationContext());
                registrationRequest.token = registrationId;
                registrationRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        GCMUtils.setRegisteredFlag(context);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                    }
                }).exec();
                Looper.loop();
            }
        }).start();
    }

    @Override
    protected void onUnregistered(Context context, String s) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private String getUserId(String user) {
        String id = "";
        JSONObject userJSON = null;
        try {
            userJSON = new JSONObject(user);
            id = userJSON.optString("id");
        } catch (JSONException e) {
            Debug.error(e);
        }

        return id;
    }
}
