package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import android.os.Bundle;

public class SettingsActivity extends BaseFragment {
    // Data
    //---------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_settings);
    }
    //---------------------------------------------------------------------------
    // FrameActivity
    //---------------------------------------------------------------------------
    @Override
    public void clearLayout() {
        Debug.log(this, "SettingsActivity::clearLayout");
    }
    //---------------------------------------------------------------------------
    @Override
    public void fillLayout() {
        Debug.log(this, "SettingsActivity::fillLayout");
    }
    //---------------------------------------------------------------------------
    @Override
    public void release() {
    }
    //---------------------------------------------------------------------------
}
