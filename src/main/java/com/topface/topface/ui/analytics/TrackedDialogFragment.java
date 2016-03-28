package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import com.topface.topface.statistics.ScreensShowStatistics;
import com.topface.topface.utils.Utils;

public class TrackedDialogFragment extends DialogFragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (isTrackable()) {
            senPopupShownStatistics();
        }
    }

    protected String getTrackName() {
        return Utils.getClassName(getClass().getSimpleName());
    }

    public void senPopupShownStatistics() {
        ScreensShowStatistics.sendPopupShow(getClass().getSimpleName());
    }

    public boolean isTrackable() {
        return true;
    }
}
