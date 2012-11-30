package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import com.google.analytics.tracking.android.EasyTracker;

public class TrackedDialogFragment extends DialogFragment {
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
