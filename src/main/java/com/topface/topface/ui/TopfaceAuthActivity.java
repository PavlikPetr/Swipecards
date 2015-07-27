package com.topface.topface.ui;

import android.os.Bundle;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.TopfaceAuthFragment;

/**
 * Activity for Topface authorization
 */
public class TopfaceAuthActivity extends NoAuthActivity<TopfaceAuthFragment> {

    public static final int INTENT_TOPFACE_AUTH = 26;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected boolean isNeedShowActionBar() {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected String getFragmentTag() {
        return TopfaceAuthFragment.class.getSimpleName();
    }

    @Override
    protected TopfaceAuthFragment createFragment() {
        return new TopfaceAuthFragment();
    }
}
