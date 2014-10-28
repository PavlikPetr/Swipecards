package com.topface.topface.ui;

import com.topface.topface.ui.fragments.TopfaceAuthFragment;

/**
 * Activity for Topface authorization
 */
public class TopfaceAuthActivity extends NoAuthActivity<TopfaceAuthFragment> {

    public static final int INTENT_TOPFACE_AUTH = 26;
    @Override
    protected String getFragmentTag() {
        return TopfaceAuthFragment.class.getSimpleName();
    }

    @Override
    protected TopfaceAuthFragment createFragment() {
        return new TopfaceAuthFragment();
    }
}
