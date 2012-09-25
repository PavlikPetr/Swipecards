package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import android.support.v4.app.FragmentManager;

public class FragmentContainer {
    private int mCurrentFragmentId;
    private FragmentManager mFragmentManager;
    
    private BaseFragment mProfileFragment;
    private BaseFragment mDatingFragment;
    private BaseFragment mLikesFragment;
    private BaseFragment mMutualFragment;
    private BaseFragment mDialogsFragment;
    private BaseFragment mTopsFragment;
    private BaseFragment mSettingsFragment;
    
    public FragmentContainer(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
//        
//        mProfileFragment = new ProfileFragment();
//        mProfileFragment.setId(R.id.fragment_profile);
//        //fragmentManager.beginTransaction().add(R.id.fragment_profile, mProfileFragment).hide(mProfileFragment).commit();
//        
//        mDatingFragment = new DatingFragment();
//        mDatingFragment.setId(R.id.fragment_dating);
//        //fragmentManager.beginTransaction().add(R.id.fragment_dating, mDatingFragment).hide(mDatingFragment).commit();
//        
//        mLikesFragment = new LikesFragment();
//        mLikesFragment.setId(R.id.fragment_likes);
//        //fragmentManager.beginTransaction().add(R.id.fragment_likes, mLikesFragment).hide(mLikesFragment).commit();
//        
//        mMutualFragment = new MutualFragment();
//        mMutualFragment.setId(R.id.fragment_mutual);
//        //fragmentManager.beginTransaction().add(R.id.fragment_mutual, mMutualFragment).hide(mMutualFragment).commit();
//        
//        mDialogsFragment = new DialogsFragment();
//        mDialogsFragment.setId(R.id.fragment_dialogs);
//        //fragmentManager.beginTransaction().add(R.id.fragment_dialogs, mDialogsFragment).hide(mDialogsFragment).commit();
//        
//        mTopsFragment = new TopsFragment();
//        mTopsFragment.setId(R.id.fragment_tops);
//        //fragmentManager.beginTransaction().add(R.id.fragment_tops, mTopsFragment).hide(mTopsFragment).commit();
//        
//        mSettingsFragment = new SettingsFragment();
//        mSettingsFragment.setId(R.id.fragment_settings);
//        //fragmentManager.beginTransaction().add(R.id.fragment_settings, mSettingsFragment).hide(mSettingsFragment).commit();
    }
    
    public void showFragment(int fragmentId) {
        showFragment(fragmentId, false);
    }
    
    public void showFragment(int fragmentId, boolean updateFragment) {
//        if(mCurrentFragmentId > 0)
//            mFragmentManager.beginTransaction().hide(mFragmentManager.findFragmentById(mCurrentFragmentId)).commit();
//        
//        BaseFragment fragment = null;
//        switch (fragmentId) {
//            case R.id.fragment_profile:
//                fragment = mProfileFragment;
//                break;
//            case R.id.fragment_dating:
//                fragment = mDatingFragment;
//                break;
//            case R.id.fragment_likes:
//                fragment = mLikesFragment;
//                break;
//            case R.id.fragment_mutual:
//                fragment = mMutualFragment;
//                break;
//            case R.id.fragment_dialogs:
//                fragment = mDialogsFragment;
//                break;
//            case R.id.fragment_tops:
//                fragment = mTopsFragment;
//                break;
//            case R.id.fragment_settings:
//                fragment = mSettingsFragment;
//                break;
//            default:
//                fragment = mProfileFragment;
//                fragmentId = R.id.fragment_profile;
//                break;
//        }
//        
//        mFragmentManager.beginTransaction().show(fragment).commit();
//        mCurrentFragmentId = fragmentId;
//        
//        if(updateFragment) {
//            //update(fragment);
//            fragment.isForcedUpdate = true;
//        }
    }
    
//    public void update(BaseFragment fragment) {    	
//        if(fragment != null && !fragment.isFilled) {
//	        fragment.fillLayout();
//	        fragment.isFilled = true;
//        }
//    }
    
    public void update() {
        BaseFragment fragment = (BaseFragment)mFragmentManager.findFragmentById(mCurrentFragmentId);
        //update(fragment);
    }
    
    public int getCurrentFragmentId() {
        return mCurrentFragmentId;
    }
}

