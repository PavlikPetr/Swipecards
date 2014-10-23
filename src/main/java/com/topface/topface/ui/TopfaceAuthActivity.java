package com.topface.topface.ui;

import com.topface.topface.ui.fragments.TopfaceAuthFragment;

/**
 * Activity for Topface authorization
 */
public class TopfaceAuthActivity extends NoAuthActivity<TopfaceAuthFragment> {
    @Override
    protected String getFragmentTag() {
        return TopfaceAuthFragment.class.getSimpleName();
    }

    @Override
    protected TopfaceAuthFragment createFragment() {
        return new TopfaceAuthFragment();
    }
}
