package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class FragmentFrameAdapter {
    private int mCurrentFragmentId;
    private FragmentManager mFragmentManager;
    
    private Fragment mFragmentProfile;
    private Fragment mFragmentDating;
    private Fragment mFragmentLikes;
    private Fragment mFragmentMutual;
    private Fragment mFragmentDialogs;
    private Fragment mFragmentTops;
    private Fragment mFragmentSettings;

    public FragmentFrameAdapter(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;

        mFragmentProfile  = new DatingActivity();
        mFragmentDating   = new DatingActivity();
        mFragmentLikes    = new LikesActivity();
        mFragmentMutual   = new SympathyActivity();
        mFragmentDialogs  = new DialogActivity();
        mFragmentTops     = new TopsActivity();
        mFragmentSettings = new SettingsActivity();
        
        fragmentManager.beginTransaction().add(R.id.fragment_profile, mFragmentProfile).hide(mFragmentProfile).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_dating, mFragmentDating).hide(mFragmentDating).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_likes, mFragmentLikes).hide(mFragmentLikes).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_mutual, mFragmentMutual).hide(mFragmentMutual).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_dialogs, mFragmentDialogs).hide(mFragmentDialogs).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_tops, mFragmentTops).hide(mFragmentTops).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_settings, mFragmentSettings).hide(mFragmentSettings).commit();
    }
    
    public void showFragment(int fragmentId) {
        if(mCurrentFragmentId > 0)
            mFragmentManager.beginTransaction().hide(mFragmentManager.findFragmentById(mCurrentFragmentId)).commit();
        mFragmentManager.beginTransaction().show(mFragmentManager.findFragmentById(fragmentId)).commit();
        mCurrentFragmentId = fragmentId;
    }
}

