package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.data.InstallReferrerData;
import com.topface.topface.utils.config.AppConfig;

/**
 * Created by ppavlik on 20.07.16.
 * catch install referrer data
 */

public class InstallReferrerReceiver extends BroadcastReceiver {
    private static final String REFERER = "referrer";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String referrer = extras != null ? extras.getString(REFERER) : null;
        if (!TextUtils.isEmpty(referrer)) {
            AppConfig appConfig = App.getAppConfig();
            appConfig.setReferrerTrackData(new InstallReferrerData(referrer));
            appConfig.saveConfig();
        }

    }
}
