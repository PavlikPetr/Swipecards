package com.topface.topface.utils;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.topface.App;
import com.topface.topface.R;

public class EasyTracker {
    private static Tracker mTracker;

    public static String SESSION_CONTROL = "session_control";

    public static Tracker getTracker() {
        if (mTracker == null) {
            Context context = App.getContext();
            Resources resources = context.getResources();
            //Задаем настройки. Делаем это не через xml, что бы lint не ругался на xml файл
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
            analytics.setDryRun(resources.getBoolean(R.bool.ga_dryRun));
            analytics.getLogger().setLogLevel(resources.getInteger(R.integer.ga_logLevelInt));

            //Создаем новый трекер и указываем его настройки
            mTracker = analytics.newTracker(context.getString(R.string.ga_trackingId));
            mTracker.enableExceptionReporting(resources.getBoolean(R.bool.ga_reportUncaughtExceptions));
            mTracker.enableAutoActivityTracking(resources.getBoolean(R.bool.ga_autoActivityTracking));
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
