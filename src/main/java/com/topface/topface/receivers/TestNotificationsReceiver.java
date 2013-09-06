package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.topface.topface.GCMUtils;
import com.topface.topface.utils.Debug;

public class TestNotificationsReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Debug.log("TOPFACE_NOT" + intent.getStringExtra("text"));
        GCMUtils.showNotification(intent, context);
    }
}
