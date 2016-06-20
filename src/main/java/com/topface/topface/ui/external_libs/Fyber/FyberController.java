package com.topface.topface.ui.external_libs.Fyber;

import android.app.Activity;

import com.fyber.Fyber;

public class FyberController {
    private static final String FYBER_ID = "11625";

    private Fyber.Settings mSettings;

    private void init(Activity activity) {
        if (mSettings == null) {
            if (activity != null) {
                Fyber.Settings mSettings = Fyber.with(FYBER_ID, activity).start();
            } else {
                throw new IllegalArgumentException("Activity cannot be null.");
            }
        }
    }
}
