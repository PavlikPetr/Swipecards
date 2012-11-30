package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.support.v4.app.Fragment;
import com.google.analytics.tracking.android.EasyTracker;

public class TrackedFragment extends Fragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isTrackable()) {
            EasyTracker.getTracker().trackView(getTrackName());
        }
    }

    protected String getTrackName() {
        return this.getClass().getSimpleName().replace("Fragment", "");
    }

    public boolean isTrackable() {
        return true;
    }
}
