package com.topface.topface.statistics;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;

public class InviteUniqueStatistics {

    public static final String CONTACTS_INVITES_UNIQUE = "contacts_invites_unique";
    public static final String FACEBOOK_INVITES_UNIQUE = "facebook_invites_unique";

    private static void sendStatistic(String key, String unique) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendUniqueEvent(key, 1, unique + "_" + key);
    }

    public static void sendContactsInvites(int amount) {
        sendStatistic(CONTACTS_INVITES_UNIQUE, Integer.toString(amount));
    }

    public static void sendFacebookInvites(int amount) {
        sendStatistic(FACEBOOK_INVITES_UNIQUE, Integer.toString(amount));
    }

}
