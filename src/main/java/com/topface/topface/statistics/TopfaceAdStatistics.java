package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.data.Banner;

/**
 * Created by kirussell on 13/01/15.
 * Sends ads statistics though util methods
 */
public class TopfaceAdStatistics {
    private static final String TF_MOBILE_BANNER_SHOW = "mobile_tf_banner_show";
    private static final String TF_MOBILE_BANNER_CLICK = "mobile_tf_banner_click";
    private static final String TF_MOBILE_FULLSCREEN_SHOW = "mobile_tf_fullscreen_show";
    private static final String TF_MOBILE_FULLSCREEN_CLICK = "mobile_tf_fullscreen_click";
    private static final String TF_MOBILE_FULLSCREEN_CLOSED = "mobile_tf_fullscreen_close";
    private static final String TF_MOBILE_NATIVE_AD_SHOW = "mobile_tf_native_ad_show";
    private static final String TF_MOBILE_NATIVE_AD_CLICK = "mobile_tf_native_ad_click";
    private static final String PUBNATIVE = "pubnative";
    private static final String TF_MOBILE_NATIVE_SHOW_FAILED = "mobile_tf_native_ad_show_failed";

    private static void send(String key, String name) {
        StatisticsTracker.getInstance()
                .sendEvent(key, 1, new Slices().putSlice(TfStatConsts.val, name));
    }

    public static void sendBannerShown(Banner banner) {
        send(TF_MOBILE_BANNER_SHOW, banner.name);
    }

    public static void sendBannerClicked(Banner banner) {
        send(TF_MOBILE_BANNER_CLICK, banner.name);
    }

    @SuppressWarnings("unused")
    public static void sendFullscreenShown(Banner banner) {
        send(TF_MOBILE_FULLSCREEN_SHOW, banner.name);
    }

    @SuppressWarnings("unused")
    public static void sendFullscreenClicked(Banner banner) {
        send(TF_MOBILE_FULLSCREEN_CLICK, banner.name);
    }

    @SuppressWarnings("unused")
    public static void sendFullscreenClosed(Banner banner) {
        send(TF_MOBILE_FULLSCREEN_CLOSED, banner.name);
    }

    public static void sendPubnativeImpression() {
        send(TF_MOBILE_NATIVE_AD_SHOW, PUBNATIVE);
    }

    public static void sendPubnativeClick() {
        send(TF_MOBILE_NATIVE_AD_CLICK, PUBNATIVE);
    }

    public static void sendPubnativeImpressionFailed() {
        send(TF_MOBILE_NATIVE_SHOW_FAILED, PUBNATIVE);
    }
}
