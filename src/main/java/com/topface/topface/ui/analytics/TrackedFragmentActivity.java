package com.topface.topface.ui.analytics;

import com.comscore.analytics.comScore;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.HitBuilders;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.statistics.ScreensShowStatistics;
import com.topface.topface.ui.IBackPressedListener;
import com.topface.topface.ui.fragments.TrackedLifeCycleActivity;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.FlurryManager;

import org.jetbrains.annotations.Nullable;

public class TrackedFragmentActivity extends TrackedLifeCycleActivity {
    private IBackPressedListener mBackPressedListener;

    @Override
    public void onStart() {
        super.onStart();
        StatisticsTracker.getInstance().activityStart(this);
        if (isTrackable()) {
            senActivitiesShownStatistics();
        }
    }

    public void senActivitiesShownStatistics() {
        ScreensShowStatistics.sendScreenShow(getClass().getSimpleName());
        FlurryManager.getInstance().sendPageOpenEvent(getScreenName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FacebookSdk.isInitialized()) {
            AppEventsLogger.activateApp(this, App.getAppSocialAppsIds().fbId);
        }
        comScore.onEnterForeground();
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getTracker().send(new HitBuilders.AppViewBuilder().set(EasyTracker.SESSION_CONTROL, "end").build());
    }

    public boolean isTrackable() {
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        comScore.onExitForeground();
        StatisticsTracker.getInstance().activityStop(this);
    }


    public void setBackPressedListener(IBackPressedListener listener) {
        mBackPressedListener = listener;
    }

    public IBackPressedListener getBackPressedListener() {
        return mBackPressedListener;
    }

    @Nullable
    protected String getScreenName() {
        return null;
    }
}
