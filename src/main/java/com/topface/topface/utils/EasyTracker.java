package com.topface.topface.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.framework.utils.Debug;
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
            mTracker = analytics.newTracker(getTrackingId());
            mTracker.enableExceptionReporting(resources.getBoolean(R.bool.ga_reportUncaughtExceptions));
            mTracker.enableAutoActivityTracking(resources.getBoolean(R.bool.ga_autoActivityTracking));
            mTracker.setSampleRate(0.5);
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

    private static String getTrackingId() {
        Context ctx = App.getContext();
        String myApiKey = "";
        try {
            ApplicationInfo ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            myApiKey = bundle.getString(ctx.getString(R.string.tracking_id_meta_data_name));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Debug.log("EasyTracker", "myApiKey: " + myApiKey);
        return myApiKey;
    }
}
