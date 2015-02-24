package com.topface.topface.statistics;

import android.text.TextUtils;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;

/**
 * Created by mbautin on 24.02.15.
 * Sends redirection statistics from sheme topface://mobvk for example
 */
public class RedirectStatistics {

    private static final String TF_MOBILE_REDIRECT = "mobile_tf_redirect";
    private static final String TF_MOBILE_REDIRECT_UNDEFINED = "undefined";

    public static void send(String host) {
        if (TextUtils.isEmpty(host)) {
            host = TF_MOBILE_REDIRECT_UNDEFINED;
        }
        StatisticsTracker.getInstance()
                .sendEvent(TF_MOBILE_REDIRECT, 1, new Slices().putSlice(TfStatConsts.val, host));

    }
}
