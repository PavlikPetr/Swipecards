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
        return ((Object) this).getClass().getSimpleName().replace("Fragment", Utils.EMPTY).replace("Dialog", Utils.EMPTY).replace("Popup", Utils.EMPTY);
    }

    public void senPopupShownStatistics() {
        ScreensShowStatistics.sendPopupShow(getTrackName());
    }

    public boolean isTrackable() {
        return true;
    }
}
