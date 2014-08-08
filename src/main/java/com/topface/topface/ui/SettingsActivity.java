package com.topface.topface.ui;

import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.SettingsFragment;

public class SettingsActivity extends CheckAuthActivity<SettingsFragment> {

    public static final int INTENT_SETTINGS = 7;

    public static Intent createIntent() {
        Intent intent = new Intent(App.getContext(), SettingsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_SETTINGS);
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return SettingsFragment.class.getSimpleName();
    }

    @Override
    protected SettingsFragment createFragment() {
        return new SettingsFragment();
    }
}
