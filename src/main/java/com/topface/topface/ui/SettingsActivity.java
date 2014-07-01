package com.topface.topface.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.SettingsFragment;

public class SettingsActivity extends CheckAuthActivity {

    public static final int INTENT_SETTINGS = 7;

    public static Intent createIntent() {
        Intent intent = new Intent(App.getContext(), SettingsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_SETTINGS);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new SettingsFragment();
    }
}
