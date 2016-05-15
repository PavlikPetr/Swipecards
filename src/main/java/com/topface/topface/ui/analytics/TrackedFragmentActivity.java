package com.topface.topface.ui.analytics;

import android.text.TextUtils;

import com.comscore.analytics.comScore;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.HitBuilders;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.data.ExperimentTags;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.IBackPressedListener;
import com.topface.topface.statistics.ScreensShowStatistics;
import com.topface.topface.ui.fragments.TrackedLifeCycleActivity;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.Nullable;

public class TrackedFragmentActivity extends TrackedLifeCycleActivity {
    private IBackPressedListener mBackPressedListener;

    @Override
    public void onStart() {
        super.onStart();
        App.onActivityStarted(this.getClass().getName());
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

    public static HitBuilders.AppViewBuilder setCustomMeticsAndDimensions(Options options, Profile profile) {
        //Дополнительные параметры для статистики
        HitBuilders.AppViewBuilder builder = new HitBuilders.AppViewBuilder();
        String socialNet = AuthToken.getInstance().getSocialNet();
        builder.setCustomDimension(1, TextUtils.isEmpty(socialNet) ? "Unauthorized" : socialNet);
        builder.setCustomDimension(2, profile.sex == 0 ? "Female" : "Male");
        builder.setCustomDimension(3, profile.paid ? "Yes" : "No");
        builder.setCustomDimension(4, profile.emailConfirmed ? "Yes" : "No");
        builder.setCustomDimension(5, profile.premium ? "Yes" : "No");
        builder.setCustomDimension(6, Integer.toString(profile.age));
        builder.set(EasyTracker.SESSION_CONTROL, "start");
        /**
         * Абстрактное поле для подсчета статистики экспериментов
         * Т.е. сервер может прислать любые данные для п
         */
        ExperimentTags tags = options.experimentTags;
        if (tags != null) {
            tags.setToStatistics(builder);
        }
        return builder;
    }

    @Override
    public void onStop() {
        super.onStop();
        App.onActivityStoped(this.getClass().getName());
        EasyTracker.getTracker().send(new HitBuilders.AppViewBuilder().set(EasyTracker.SESSION_CONTROL, "end").build());
    }

    public boolean isTrackable() {
        return true;
    }

    protected String getTrackName() {
        return Utils.getClassName(getClass().getSimpleName());
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
