package com.topface.topface.ui;

import android.support.v7.app.ActionBarActivity;

import com.topface.topface.data.Options;
import com.topface.topface.state.OptionsProvider;

/**
 * Активити для распространениия опций и профиля через AppState
 * Created by onikitin on 18.08.15.
 */
@SuppressWarnings("deprecation")
public class StateTaransportFragmentActivity extends ActionBarActivity implements OptionsProvider.IOptionsUpdater {

    private OptionsProvider optionsProvider = new OptionsProvider(this);
    private Options mOptions;

    @Override
    public Options getOptions() {
        return mOptions;
    }

    @Override
    public void onOptionsUpdate(Options options) {
        mOptions = options;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        optionsProvider.unsubscribe();
    }


}
