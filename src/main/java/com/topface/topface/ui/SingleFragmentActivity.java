package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.topface.topface.R;

public abstract class SingleFragmentActivity extends CustomTitlesBaseFragmentActivity {

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        mFragment = createFragment();
        setArguments();
        addToLayout();
    }

    protected int addToLayout() {
        return getSupportFragmentManager().beginTransaction().add(getContainerId(), mFragment).commit();
    }

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
