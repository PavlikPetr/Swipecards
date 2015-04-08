package com.topface.topface.ui;

import com.topface.topface.ui.fragments.gift.OwnGiftsFragment;
import com.topface.topface.utils.CacheProfile;

/**
 * Created by saharuk on 06.04.15.
 */
public class OwnGiftsActivity extends CheckAuthActivity<OwnGiftsFragment> {
    @Override
    protected String getFragmentTag() {
        return OwnGiftsFragment.class.getName();
    }

    @Override
    protected OwnGiftsFragment createFragment() {
        OwnGiftsFragment ownGiftsFragment = new OwnGiftsFragment();
        ownGiftsFragment.setProfile(CacheProfile.getProfile());
        return ownGiftsFragment;
    }
}
