package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.topface.topface.R;

public abstract class SingleFragmentActivity extends CustomTitlesBaseFragmentActivity {

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        FragmentManager fm = getSupportFragmentManager();
        Fragment oldFragment = fm.findFragmentByTag(getFragmentTag());
        if (oldFragment != null) {
            mFragment = oldFragment;
        } else {
            mFragment = createFragment();
            setArguments();
        }
        addToLayout();
    }

    protected void addToLayout() {
        if (!mFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().add(getContainerId(), mFragment, getFragmentTag()).commit();
        }
    }

    protected abstract String getFragmentTag();

    protected int getContainerId() {
        return R.id.loFrame;
    }

    protected void setArguments() {
        mFragment.setArguments(getIntent().getExtras());
    }

    protected void initLayout() {
        setContentView(R.layout.ac_fragment_frame);
    }

    protected Fragment getFragment() {
        return mFragment;
    }

    protected abstract Fragment createFragment();
}
