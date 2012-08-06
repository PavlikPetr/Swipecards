package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProfileFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
      View view = inflater.inflate(R.layout.ac_settings, null);  

        return view;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void clearLayout() {
        Debug.log(this, "SettingsActivity::clearLayout");
    }

    @Override
    public void fillLayout() {
        Debug.log(this, "SettingsActivity::fillLayout");
    }

}
