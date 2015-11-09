package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.utils.CacheProfile;

/**
 * Sending unique statistics about push button "Buy VIP".
 */
public class ExpressMessagesUniqueStatistics {

    public static final String OPEN_EXPRESS_MESSAGES_UNIQUE = "open_express_messages_unique";

    public static void send() {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendUniqueEvent(OPEN_EXPRESS_MESSAGES_UNIQUE, 1, ExpressMessagesStastics.getSlices(), Integer.toString(CacheProfile.getProfile().uid) + "_" + OPEN_EXPRESS_MESSAGES_UNIQUE);
    }
}