package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.topface.App;

public class TrackedDialogFragment extends DialogFragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isTrackable()) {
            Tracker tracker = App.getTracker();
            tracker.setScreenName(getTrackName());
            tracker.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    protected String getTrackName() {
        return ((Object) this).getClass().getSimpleName().replace("Fragment", "");
    }

    public boolean isTrackable() {
        return true;
    }
}
