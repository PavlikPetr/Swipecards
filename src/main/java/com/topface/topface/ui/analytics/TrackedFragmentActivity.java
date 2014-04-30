package com.topface.topface.ui.analytics;

import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.topface.topface.data.ExperimentTags;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;

public class TrackedFragmentActivity extends ActionBarActivity {


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        setCustomMeticsAndDimensions();
        if (isTrackable()) {
            EasyTracker.getTracker().sendView(getTrackName());
        }
    }

    public static void setCustomMeticsAndDimensions() {
        //Дополнительные параметры для статистики
        Tracker tracker = EasyTracker.getTracker();

        String socialNet = AuthToken.getInstance().getSocialNet();
        tracker.setCustomDimension(1, TextUtils.isEmpty(socialNet) ? "Unauthorized" : socialNet);
        tracker.setCustomDimension(2, CacheProfile.sex == 0 ? "Female" : "Male");
        tracker.setCustomDimension(3, CacheProfile.paid ? "Yes" : "No");
        tracker.setCustomDimension(4, CacheProfile.emailConfirmed ? "Yes" : "No");
        tracker.setCustomDimension(5, CacheProfile.premium ? "Yes" : "No");
        tracker.setCustomDimension(6, Integer.toString(CacheProfile.age));
        /**
         * Абстрактное поле для подсчета статистики экспериментов
         * Т.е. сервер может прислать любые данные для п
         */
        ExperimentTags tags = CacheProfile.getOptions().experimentTags;
        if (tags != null) {
            tags.setToStatistics(tracker);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    public boolean isTrackable() {
        return true;
    }

    protected String getTrackName() {
        return ((Object) this).getClass().getSimpleName().replace("Activity", "");
    }
}
