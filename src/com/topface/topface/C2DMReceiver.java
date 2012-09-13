package com.topface.topface;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import com.google.android.c2dm.C2DMBaseReceiver;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.RegistrationTokenRequest;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;

public class C2DMReceiver extends C2DMBaseReceiver {
    // Data
    public static final String SENDER_ID = "android@topface.com";
    //---------------------------------------------------------------------------
    public C2DMReceiver() {
        super(SENDER_ID);
    }
    //---------------------------------------------------------------------------
    @Override
    public void onRegistered(final Context context,final String registrationId) {
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
                        C2DMUtils.setRegisteredFlag(context);
                    }
                    @Override
                    public void fail(int codeError,ApiResponse response) {
                    }
                }).exec();
                Looper.loop();
            }
        }).start();
    }
    //---------------------------------------------------------------------------
    @Override
    public void onUnregistered(Context context) {
        Debug.log("onUnregistered", "");
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onMessage(Context context,Intent receiveIntent) {
    if (Settings.getInstance().isNotificationEnabled()) {
        C2DMUtils.showNotification(receiveIntent, context);
    }
        //Сообщаем о том что есть новое уведомление и нужно обновить список игр
        Intent broadcastReceiver = new Intent(C2DMUtils.C2DM_NOTIFICATION);
    broadcastReceiver.putExtra("id", receiveIntent.getStringExtra("id"));
        context.sendBroadcast(broadcastReceiver);
    }
    //---------------------------------------------------------------------------
    @Override
    public void onError(Context context,String errorId) {
        Debug.log("onError", errorId);
    }
    //---------------------------------------------------------------------------
}
