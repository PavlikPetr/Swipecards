package com.topface.topface.ui.analytics;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.maps.MapActivity;

abstract public class TrackedMapActivity extends MapActivity {
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
