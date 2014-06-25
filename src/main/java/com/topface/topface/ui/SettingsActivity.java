package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;

public class SettingsActivity extends CheckAuthActivity {

    public static final int INTENT_SETTINGS = 7;

    public static Intent getSettingsIntent() {
        Intent intent = new Intent(App.getContext(), SettingsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_SETTINGS);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ac_settings);
    }
}
