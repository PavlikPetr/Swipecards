package com.topface.topface.ui.profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class UserProfilePageAdapter extends FragmentPagerAdapter {

    public UserProfilePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new PhotoFragment();
            case 1:
                return new QuestionnaireFragment();
            case 2:
                return new GiftsFragment();
            case 3:
                return new ActionsFragment();
        }
        return null;
    }
}
