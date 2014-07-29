package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.topface.App;
import com.topface.topface.utils.EasyTracker;

public class TrackedFragment extends Fragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isTrackable()) {
            Tracker tracker = EasyTracker.getTracker();
            tracker.setScreenName(getTrackName());
            tracker.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    public String getTrackName() {
        return ((Object) this).getClass().getSimpleName().replace("Fragment", "");
    }

    public boolean isTrackable() {
        return true;
    }
}
