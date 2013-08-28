package com.topface.topface.ui.analytics;

import android.support.v7.app.ActionBarActivity;
import com.google.analytics.tracking.android.EasyTracker;

public class TrackedFragmentActivity extends ActionBarActivity {


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        TrackedActivity.setCustomMeticsAndDimensions();
        if (isTrackable()) {
            EasyTracker.getTracker().trackView(getTrackName());
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
        return this.getClass().getSimpleName().replace("Activity", "");
    }
}
