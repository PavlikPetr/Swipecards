package com.topface.topface.ui.analytics;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.comscore.analytics.comScore;
import com.google.android.gms.analytics.HitBuilders;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.data.ExperimentTags;
import com.topface.topface.statistics.ScreensShowStatistics;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

public class TrackedFragmentActivity extends AppCompatActivity {

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        comScore.onEnterForeground();
    }

    public static HitBuilders.AppViewBuilder setCustomMeticsAndDimensions() {
        //Дополнительные параметры для статистики
        HitBuilders.AppViewBuilder builder = new HitBuilders.AppViewBuilder();
        String socialNet = AuthToken.getInstance().getSocialNet();
        builder.setCustomDimension(1, TextUtils.isEmpty(socialNet) ? "Unauthorized" : socialNet);
        builder.setCustomDimension(2, CacheProfile.sex == 0 ? "Female" : "Male");
        builder.setCustomDimension(3, CacheProfile.paid ? "Yes" : "No");
        builder.setCustomDimension(4, CacheProfile.emailConfirmed ? "Yes" : "No");
        builder.setCustomDimension(5, CacheProfile.premium ? "Yes" : "No");
        builder.setCustomDimension(6, Integer.toString(CacheProfile.age));
        builder.set(EasyTracker.SESSION_CONTROL, "start");
        /**
         * Абстрактное поле для подсчета статистики экспериментов
         * Т.е. сервер может прислать любые данные для п
         */
        ExperimentTags tags = CacheProfile.getOptions().experimentTags;
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
}
