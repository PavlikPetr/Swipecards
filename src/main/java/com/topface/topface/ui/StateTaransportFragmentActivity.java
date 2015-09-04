package com.topface.topface.ui;


import android.support.v7.app.ActionBarActivity;

import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.state.IStateDataUpdater;
import com.topface.topface.state.OptionsAndProfileProvider;

/**
 * Активити для распространениия опций и профиля через AppState
 * Created by onikitin on 18.08.15.
 */
@SuppressWarnings("deprecation")
public class StateTaransportFragmentActivity extends ActionBarActivity implements IStateDataUpdater {

    private OptionsAndProfileProvider mProvider = new OptionsAndProfileProvider(this);
    private Options mOptions;
    private Profile mProfile;

    @Override
    public void onProfileUpdate(Profile profile) {
        mProfile = profile;
    }

    @Override
    public Profile getProfile() {
        return mProfile;
    }

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
        mProvider.unsubscribe();
    }


}
