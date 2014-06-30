package com.topface.topface.utils;

import com.google.android.gms.analytics.HitBuilders;
import com.topface.topface.App;

public class EasyTracker {
    public static void sendEvent(String category, String action, String label, long value) {
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setCategory(category);
        eventBuilder.setAction(action);
        eventBuilder.setLabel(label);
        eventBuilder.setValue(value);
        App.getTracker().send(eventBuilder.build());
    }
}
