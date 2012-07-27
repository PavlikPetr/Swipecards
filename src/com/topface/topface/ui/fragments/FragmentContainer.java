package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class FragmentContainer {
    private int mCurrentFragmentId;
    private FragmentManager mFragmentManager;
    
    private Fragment mFragmentProfile;
    private Fragment mFragmentDating;
    private Fragment mFragmentLikes;
    private Fragment mFragmentMutual;
    private Fragment mFragmentDialogs;
    private Fragment mFragmentTops;
    private Fragment mFragmentSettings;

    public FragmentContainer(int fragmentId, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;

        mFragmentProfile  = new DatingFragment();
        mFragmentDating   = new DatingFragment();
        mFragmentLikes    = new LikesFragment();
        mFragmentMutual   = new MutualFragment();
        mFragmentDialogs  = new DialogsFragment();
        mFragmentTops     = new TopsFragment();
        mFragmentSettings = new SettingsFragment();
        

        fragmentManager.beginTransaction().add(R.id.fragment_profile, mFragmentProfile).hide(mFragmentProfile).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_dating, mFragmentDating).hide(mFragmentDating).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_likes, mFragmentLikes).hide(mFragmentLikes).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_mutual, mFragmentMutual).hide(mFragmentMutual).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_dialogs, mFragmentDialogs).hide(mFragmentDialogs).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_tops, mFragmentTops).hide(mFragmentTops).commit();
        fragmentManager.beginTransaction().add(R.id.fragment_settings, mFragmentSettings).hide(mFragmentSettings).commit();
        
        
//        switch (fragmentId) {
//            case R.id.fragment_profile:
//                break;
//            case R.id.fragment_dating:
//                break;
//            case R.id.fragment_likes:
//                break;
//            case R.id.fragment_mutual:
//                break;
//            case R.id.fragment_dialogs:
//                break;
//            case R.id.fragment_tops:
//                break;
//            case R.id.fragment_settings:
//                break;
//            default:
//                break;
//        }
        
        mFragmentManager.beginTransaction().show(mFragmentManager.findFragmentById(fragmentId)).commit();
        
        mCurrentFragmentId = fragmentId;
    }
    
    public void showFragment(int fragmentId) {
        mFragmentManager.beginTransaction().hide(mFragmentManager.findFragmentById(mCurrentFragmentId)).commit();
        mFragmentManager.beginTransaction().show(mFragmentManager.findFragmentById(fragmentId)).commit();
        mCurrentFragmentId = fragmentId;
    }
}

