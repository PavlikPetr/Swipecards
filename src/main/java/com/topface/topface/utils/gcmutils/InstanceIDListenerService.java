package com.topface.topface.utils.gcmutils;

import android.content.Intent;

import com.topface.framework.utils.Debug;

public class InstanceIDListenerService extends com.google.android.gms.iid.InstanceIDListenerService {

    @Override
    public void onTokenRefresh() {
        Debug.log("GCM_registration_token: Token Refresh");
        Intent intent = new Intent(this, RegistrationService.class);
        startService(intent);
    }
}
