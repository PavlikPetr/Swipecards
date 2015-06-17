package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;

public abstract class SingleFragmentActivity<T extends Fragment> extends BaseFragmentActivity {

    private T mFragment;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mFragment != null) {
                    mFragment.onOptionsItemSelected(item);
                }
                if (isTaskRoot()) {
                    startActivity(getSupportParentActivityIntent());
                    finish();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void addToLayout() {
        if (!mFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().add(getContainerId(), mFragment, getFragmentTag()).commit();
        } else {
            Debug.log(mFragment, "Fragment was already added to activity " + ((Object) this).getClass().getSimpleName());
        }
    }

    protected abstract String getFragmentTag();

    protected int getContainerId() {
        return R.id.loFrame;
    }

    protected void setArguments() {
        mFragment.setArguments(getIntent().getExtras());
    }

    @Override
    protected int getContentLayout() {
        // this layout (R.layout.ac_fragment_frame) defines its own background,
        // so windowBackground in some activities (e.g. ChatActivity) defined in themes
        // doesn't work properly - overlayed by fragment background
        // so ChatActivity now has its own fragment frame layout
        return R.layout.ac_fragment_frame;
    }

    protected T getFragment() {
        return mFragment;
    }

    protected abstract T createFragment();
}
