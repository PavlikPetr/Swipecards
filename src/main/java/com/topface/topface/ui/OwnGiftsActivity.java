package com.topface.topface.ui;

import com.topface.topface.App;
import com.topface.topface.ui.fragments.gift.OwnGiftsFragment;

/**
 * Created by saharuk on 06.04.15.
 * Activity to display gifts
 */
public class OwnGiftsActivity extends CheckAuthActivity<OwnGiftsFragment> {
    @Override
    protected String getFragmentTag() {
        return OwnGiftsFragment.class.getName();
    }

    @Override
    protected OwnGiftsFragment createFragment() {
        OwnGiftsFragment ownGiftsFragment = new OwnGiftsFragment();
        ownGiftsFragment.setProfile(App.from(this).getProfile());
        return ownGiftsFragment;
    }
}
