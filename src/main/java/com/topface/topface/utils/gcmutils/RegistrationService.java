package com.topface.topface.utils.gcmutils;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.topface.framework.utils.Debug;

import java.io.IOException;

/**
 * сервис для регистрации токена на сервере
 * Created by tiberal on 04.06.16.
 */
public class RegistrationService extends IntentService {

    private static final String GCM_SENDER_ID = "932206034265";
    private static final String TAG = "GCM_REG_TOKEN";

    public RegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Debug.log("GCM_registration_token: onHandleIntent ");
            new GCMUtils(getApplicationContext()).registerGcmToken(InstanceID.getInstance(getApplicationContext())
                    .getToken(GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
