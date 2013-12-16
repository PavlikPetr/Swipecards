package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import com.google.analytics.tracking.android.EasyTracker;

public class TrackedDialogFragment extends DialogFragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EasyTracker.getInstance().setContext(getActivity());
        if (isTrackable()) {
            EasyTracker.getTracker().sendView(getTrackName());
        }
    }

    protected String getTrackName() {
        return ((Object) this).getClass().getSimpleName().replace("Fragment", "");
    }

    public boolean isTrackable() {
        return true;
    }
}
