package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

/**
 * Promo popup show unique statistics
 */
public class PromoDialogUniqueStatistics {

    public static final String PROMO_DIALOG_SHOW_UNIQUE = "promo_dialog_show_unique";

    public static void send(String tag) {
        StatisticsTracker
                .getInstance()
                .setContext(App.getContext())
                .sendUniqueEvent(PROMO_DIALOG_SHOW_UNIQUE, 1, PromoDialogStatistics.getSlices(tag), Integer.toString(App.get().getProfile().uid) + "_" + tag);
    }
}