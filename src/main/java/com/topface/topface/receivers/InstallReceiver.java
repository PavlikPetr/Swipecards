package com.topface.topface.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adjust.sdk.AdjustReferrerReceiver;
import com.appsflyer.MultipleInstallBroadcastReceiver;
import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.sponsorpay.advertiser.InstallReferrerReceiver;
import com.yandex.metrica.MetricaEventHandler;

/**
 * Created by ppetr on 30.03.16.
 * Send onReceive to all receiver with filter INSTALL_REFERRER
 */
public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Adjust
        new AdjustReferrerReceiver().onReceive(context, intent);

        // Google Analytics
        new CampaignTrackingReceiver().onReceive(context, intent);

        // Appsflyer
        new MultipleInstallBroadcastReceiver().onReceive(context, intent);

        // Sponsorpay
        new InstallReferrerReceiver().onReceive(context, intent);

        // Yandex metrica
        new MetricaEventHandler().onReceive(context, intent);
    }
}