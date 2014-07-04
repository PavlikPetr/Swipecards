package com.topface.topface.utils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.topface.App;
import com.topface.topface.R;

public class EasyTracker {
    private static Tracker mTracker;

    public static Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(App.getContext());
            mTracker = analytics.newTracker(R.xml.ga);
        }
        return mTracker;
    }

    public static void sendEvent(String category, String action, String label, long value) {
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setCategory(category);
        eventBuilder.setAction(action);
        eventBuilder.setLabel(label);
        eventBuilder.setValue(value);
        getTracker().send(eventBuilder.build());
    }
}
