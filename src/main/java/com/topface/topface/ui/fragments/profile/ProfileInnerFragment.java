package com.topface.topface.ui.fragments.profile;

import android.os.Bundle;

import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Created by kirussell on 18.03.14.
 * Fragments which contained in AbstractProfileFragment
 */
public class ProfileInnerFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(false);
    }

    @Override
    protected boolean needOptionsMenu() {
        return false;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
