package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;

public abstract class SingleFragmentActivity<T extends Fragment> extends CustomTitlesBaseFragmentActivity {

    private T mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        FragmentManager fm = getSupportFragmentManager();
        Fragment oldFragment = fm.findFragmentByTag(getFragmentTag());
        if (oldFragment != null) {
            mFragment = (T) oldFragment;
        } else {
            mFragment = createFragment();
            setArguments();
        }
        addToLayout();
    }

    protected void addToLayout() {
        if (!mFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().add(getContainerId(), mFragment, getFragmentTag()).commit();
        } else {
            Debug.log(mFragment, "Fragment was already added to activity " + getClass().getSimpleName());
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

    protected T getFragment() {
        return mFragment;
    }

    protected abstract T createFragment();
}
