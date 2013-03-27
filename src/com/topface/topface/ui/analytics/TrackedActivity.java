package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.text.TextUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;

public class TrackedActivity extends Activity {
    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        setCustomMeticsAndDimensions();

        if (isTrackable()) {
            EasyTracker.getTracker().trackView(getTrackName());
        }
    }

    public static void setCustomMeticsAndDimensions() {
        //Дополнительные параметры для статистики
        Tracker tracker = EasyTracker.getTracker();
        tracker.setCustomMetric(1, (long) CacheProfile.age);
        tracker.setCustomMetric(2, (long) CacheProfile.background_id);
        tracker.setCustomMetric(3, (long) CacheProfile.money);

        String socialNet = AuthToken.getInstance().getSocialNet();
        tracker.setCustomDimension(1, TextUtils.isEmpty(socialNet) ? "Unauthorized" : socialNet);
        tracker.setCustomDimension(2, CacheProfile.sex == 0 ? "Female" : "Male");
        tracker.setCustomDimension(3, CacheProfile.paid ? "Yes" : "No");
        tracker.setCustomDimension(4, CacheProfile.emailConfirmed ? "Yes" : "No");
        tracker.setCustomDimension(5, CacheProfile.premium ? "Yes" : "No");
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
        return this.getClass().getSimpleName().replace("Activity", "");
    }
}
