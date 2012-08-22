package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

public class FragmentContainer {
    private int mCurrentFragmentId;
    private FragmentManager mFragmentManager;
    
    private BaseFragment mDatingFragment;
    private BaseFragment mLikesFragment;
    private BaseFragment mMutualFragment;
    private BaseFragment mDialogsFragment;
    private BaseFragment mTopsFragment;
    private BaseFragment mSettingsFragment;
    
    public FragmentContainer(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        
        mDatingFragment = new DatingFragment();        
        fragmentManager.beginTransaction().add(R.id.fragment_profile, mDatingFragment).hide(mDatingFragment).commit();
        
        mDatingFragment = new DatingFragment();
        fragmentManager.beginTransaction().add(R.id.fragment_dating, mDatingFragment).hide(mDatingFragment).commit();
        
        mLikesFragment = new LikesFragment();
        fragmentManager.beginTransaction().add(R.id.fragment_likes, mLikesFragment).hide(mLikesFragment).commit();
        
        mMutualFragment = new MutualFragment();
        fragmentManager.beginTransaction().add(R.id.fragment_mutual, mMutualFragment).hide(mMutualFragment).commit();
        
        mDialogsFragment = new DialogsFragment();
        fragmentManager.beginTransaction().add(R.id.fragment_dialogs, mDialogsFragment).hide(mDialogsFragment).commit();
        
        mTopsFragment = new TopsFragment();
        fragmentManager.beginTransaction().add(R.id.fragment_tops, mTopsFragment).hide(mTopsFragment).commit();
        
        mSettingsFragment = new SettingsFragment();
        fragmentManager.beginTransaction().add(R.id.fragment_settings, mSettingsFragment).hide(mSettingsFragment).commit();

    }
    
    public void showFragment(int fragmentId) {
        if(mCurrentFragmentId > 0)
            mFragmentManager.beginTransaction().hide(mFragmentManager.findFragmentById(mCurrentFragmentId)).commit();
        
        BaseFragment fragment = null;
        switch (fragmentId) {
            case R.id.fragment_profile:
                fragment = mDatingFragment; 
                break;
            case R.id.fragment_dating:
                fragment = mDatingFragment;
                break;
            case R.id.fragment_likes:
                fragment = mLikesFragment;
                break;
            case R.id.fragment_mutual:
                fragment = mMutualFragment;
                break;
            case R.id.fragment_dialogs:
                fragment = mDialogsFragment;
                break;
            case R.id.fragment_tops:
                fragment = mTopsFragment;
                break;
            case R.id.fragment_settings:
                fragment = mSettingsFragment;
                break;
            default:
                break;
        }
        
        
        Toast.makeText(fragment.getActivity(), ""+fragment, Toast.LENGTH_SHORT).show();
        
        mFragmentManager.beginTransaction().show(fragment).commit();
        mCurrentFragmentId = fragmentId;
    }
    
    public void update() {
        BaseFragment fragment = (BaseFragment)mFragmentManager.findFragmentById(mCurrentFragmentId);
        
        if(!fragment.isFilled) {
	        fragment.fillLayout();
	        fragment.isFilled = true;
        }
    }
    
}

