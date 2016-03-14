package com.topface.topface.ui.analytics;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.topface.framework.utils.Debug;
import com.topface.topface.ui.fragments.feed.IFeedLifeCycle;
import com.topface.topface.utils.Utils;

public class TrackedFragment extends Fragment implements IFeedLifeCycle {
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (isTrackable()) {
            senFragmentShownStatistics();
        }
    }

    public String getTrackName() {
        return ((Object) this).getClass().getSimpleName().replace("Fragment", Utils.EMPTY);
    }

    public void senFragmentShownStatistics() {
        Debug.error("TrackOnResume " + getTrackName());
    }

    public boolean isTrackable() {
        return true;
    }

    @Override
    public void onResumeFragment() {
        senFragmentShownStatistics();
    }
}
