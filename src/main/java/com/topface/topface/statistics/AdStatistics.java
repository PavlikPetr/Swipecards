
package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;

public class AdStatistics {
    private static final String BANNER_SHOW = "mobile_%s_banner_show";
    private static final String BANNER_CLICK = "mobile_%s_banner_click";
    private static final String FULLSCREEN_SHOW = "mobile_%s_fullscreen_show";
    private static final String FULLSCREEN_CLICK = "mobile_%s_fullscreen_click";
    private static final String FULLSCREEN_CLOSED = "mobile_%s_fullscreen_close";

    private static void send(String command, String bannerName) {
        if (bannerName != null) {
            StatisticsTracker.getInstance()
                    .sendEvent(String.format(command, bannerName.toLowerCase()), 1);
        }
    }

    public static void sendBannerShown(String bannerName) {
        send(BANNER_SHOW, bannerName);
    }

    public static void sendBannerClicked(String bannerName) {
        send(BANNER_CLICK, bannerName);
    }

    public static void sendFullscreenShown(String bannerName) {
        send(FULLSCREEN_SHOW, bannerName);
    }

    public static void sendFullscreenClicked(String bannerName) {
        send(FULLSCREEN_CLICK, bannerName);
    }

    public static void sendFullscreenClosed(String bannerName) {
        send(FULLSCREEN_CLOSED, bannerName);
    }
}

