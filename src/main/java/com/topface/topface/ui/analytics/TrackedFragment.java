package com.topface.topface.ui.analytics;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ViewConfiguration;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.topface.topface.utils.EasyTracker;

import java.lang.reflect.Field;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ViewConfiguration config = ViewConfiguration.get(getActivity());
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
        }
    }


}
