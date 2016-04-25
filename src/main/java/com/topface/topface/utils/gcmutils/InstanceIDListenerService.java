package com.topface.topface.utils.gcmutils;

import android.content.Context;
import android.os.Looper;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;

import java.util.concurrent.atomic.AtomicBoolean;

public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {

    private static final String GCM_SENDER_ID = "932206034265";
    private static AtomicBoolean mIsListenerStarted = new AtomicBoolean(false);


    public static void getToken(final Context context) {
        if (App.isGmsEnabled()) {
            new BackgroundThread() {
                @Override
                public void execute() {
                    Looper.prepare();
                    try {
                        new GCMUtils(context).registerGcmToken(InstanceID.getInstance(context)
                                .getToken(GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null));
                        mIsListenerStarted.set(true);
                    } catch (Exception e) {
                        Debug.log(e.toString());
                    }
                    Looper.loop();
                }
            };
        }
    }

    public static boolean isListenerStarted() {
        return mIsListenerStarted.get();
    }

    @Override
    public void onTokenRefresh() {
        Debug.log("Token Obtained");
        getToken(getApplicationContext());
    }
}
