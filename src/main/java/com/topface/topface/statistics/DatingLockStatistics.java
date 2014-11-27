package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Created by onikitin on 27.11.14.
 */
public class DatingLockStatistics {

    public static final String DATING_LOCK_POPUP_SHOW = "dating_lock_popup_show";
    public static final String DATING_LOCK_POPUP_CLOSE = "dating_lock_popup_close";
    public static final String DATING_LOCK_POPUP_REDIRECT = "dating_lock_popup_redirect";

    private static void send(String key) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendEvent(key, 1);
    }

    public static void sendDatingPopupClose() {
        send(DATING_LOCK_POPUP_CLOSE);
    }

    public static void sendDatingPopupRedirect() {
        send(DATING_LOCK_POPUP_REDIRECT);
    }

    public static void sendDatingPopupShow() {
        send(DATING_LOCK_POPUP_SHOW);
    }

}
