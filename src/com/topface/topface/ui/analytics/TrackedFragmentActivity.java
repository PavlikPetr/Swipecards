package com.topface.topface.ui.analytics;

import android.support.v4.app.FragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;

public class TrackedFragmentActivity extends FragmentActivity {


    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
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
