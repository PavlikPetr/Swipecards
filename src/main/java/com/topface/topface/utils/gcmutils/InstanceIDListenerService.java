package com.topface.topface.utils.gcmutils;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;

public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {

    public static final String GCM_SENDER_ID = "932206034265";

    @Override
    public void onTokenRefresh() {
        if (App.isGmsEnabled()) {
            new BackgroundThread() {
                @Override
                public void execute() {
                    try {
                        Context context = App.getContext();
                        new GCMUtils(context).registerGcmToken(InstanceID.getInstance(context).getToken(GCM_SENDER_ID,
                                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null));
                    } catch (Exception e) {
                        Debug.log(e.toString());
                    }
                }
            };
        }
    }
}
