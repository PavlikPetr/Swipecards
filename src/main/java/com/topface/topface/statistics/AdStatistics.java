
package com.topface.topface.statistics;

import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;

public class AdStatistics {
    private static final String BANNER_SHOW = "mobile_%s_banner_show";
    private static final String BANNER_CLICK = "mobile_%s_banner_click";
    private static final String BANNER_FAIL = "mobile_%s_banner_fail";
    private static final String FULLSCREEN_SHOW = "mobile_%s_fullscreen_show";
    private static final String FULLSCREEN_CLICK = "mobile_%s_fullscreen_click";
    private static final String FULLSCREEN_CLOSED = "mobile_%s_fullscreen_close";
    private static final String FULLSCREEN_FAIL = "mobile_%s_fullscreen_fail";
    private static final String CODE_ERROR = "val";

    private static void send(String command, String bannerName, Slices slices) {
        if (bannerName != null) {
            StatisticsTracker.getInstance()
                    .sendEvent(String.format(command, bannerName.toLowerCase()), 1, slices);
        }
    }

    public static void sendBannerFailedToLoad(String bannerName, Integer codeError) {
        send(BANNER_FAIL, bannerName, codeError == null ? null : generateSlices(codeError));
    }

    public static void sendBannerShown(String bannerName) {
        send(BANNER_SHOW, bannerName, null);
    }

    public static void sendBannerClicked(String bannerName) {
        send(BANNER_CLICK, bannerName, null);
    }

    public static void sendFullscreenShown(String bannerName) {
        send(FULLSCREEN_SHOW, bannerName, null);
    }

    public static void sendFullscreenClicked(String bannerName) {
        send(FULLSCREEN_CLICK, bannerName, null);
    }

    public static void sendFullscreenClosed(String bannerName) {
        send(FULLSCREEN_CLOSED, bannerName, null);
    }

    public static void sendFullscreenFailedToLoad(String bannerName, Integer codeError) {
        send(FULLSCREEN_FAIL, bannerName, codeError == null ? null : generateSlices(codeError));
    }

    private static Slices generateSlices(int value) {
        return new Slices()
                .putSlice(CODE_ERROR, String.valueOf(value));
    }
}

