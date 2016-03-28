package com.topface.topface.ui.analytics;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.topface.topface.statistics.ScreensShowStatistics;
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
        return Utils.getClassName(getClass().getSimpleName());
    }

    public void senFragmentShownStatistics() {
        ScreensShowStatistics.sendScreenShow(getClass().getSimpleName());
    }

    public boolean isTrackable() {
        return true;
    }

    @Override
    public void onResumeFragment() {
        senFragmentShownStatistics();
    }
}
