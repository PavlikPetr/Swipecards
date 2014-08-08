package com.topface.topface.ui.analytics;

import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.topface.data.ExperimentTags;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.social.AuthToken;

public class TrackedFragmentActivity extends ActionBarActivity {


    @Override
    public void onStart() {
        super.onStart();
        if (isTrackable()) {
            Tracker tracker = EasyTracker.getTracker();
            tracker.setScreenName(getTrackName());
            tracker.send(setCustomMeticsAndDimensions().build());
        }
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
        EasyTracker.getTracker().send(new HitBuilders.AppViewBuilder().set(EasyTracker.SESSION_CONTROL, "end").build());
    }

    public boolean isTrackable() {
        return true;
    }

    protected String getTrackName() {
        return ((Object) this).getClass().getSimpleName().replace("Activity", "");
    }

}
