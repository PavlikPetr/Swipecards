package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.topface.App;

public class TrackedFragment extends Fragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        App.getTracker().getContext(getActivity());
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
