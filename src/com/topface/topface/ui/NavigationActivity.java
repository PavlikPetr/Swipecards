package com.topface.topface.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.FragmentMenu;
import com.topface.topface.ui.fragments.FragmentMenu.FragmentMenuListener;
import com.topface.topface.ui.fragments.FragmentSwitcher;
import com.topface.topface.ui.fragments.FragmentSwitcher.FragmentSwitchListener;
import com.topface.topface.utils.AuthorizationManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.ProfileBackgrounds;

public class NavigationActivity extends FragmentActivity implements View.OnClickListener {

    private FragmentManager mFragmentManager;
    private FragmentMenu mFragmentMenu;
    private FragmentSwitcher mFragmentSwitcher;
    private SharedPreferences mPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_navigation);
		Debug.log(this, "onCreate");
		
		AuthorizationManager.getInstance(this).extendAccessToken();
		
		mFragmentManager = getSupportFragmentManager();
		
		mFragmentMenu = (FragmentMenu)mFragmentManager.findFragmentById(R.id.fragment_menu);
		mFragmentMenu.setOnMenuListener(mOnFragmentMenuListener);

	    mFragmentSwitcher = (FragmentSwitcher)findViewById(R.id.fragment_switcher);
	    mFragmentSwitcher.setFragmentSwitchListener(mFragmentSwitchListener);
	    mFragmentSwitcher.setFragmentManager(mFragmentManager);

	    mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        int lastFragmentId = mPreferences.getInt(Static.PREFERENCES_NAVIGATION_LAST_FRAGMENT, BaseFragment.F_PROFILE);
        CacheProfile.background_id = mPreferences.getInt(Static.PREFERENCES_PROFILE_BACKGROUND_ID, ProfileBackgrounds.DEFAULT_BACKGROUND_ID);
	    
	    mFragmentSwitcher.showFragment(lastFragmentId);
	}

    @Override
    public void onClick(View view) {
        if(view.getId() != R.id.btnNavigationHome)
            return;
        if (mFragmentSwitcher.getAnimationState() == FragmentSwitcher.EXPAND) {
            mFragmentSwitcher.closeMenu();
        } else {
            mFragmentMenu.refreshNotifications();
            mFragmentSwitcher.openMenu();
        } 
    }
    
    @Override
    protected void onDestroy() {
        mPreferences.edit()
            .putInt(Static.PREFERENCES_NAVIGATION_LAST_FRAGMENT, mFragmentSwitcher.getCurrentFragmentId())
            .commit();
        super.onDestroy();
    }
    
    @Override
    public void onBackPressed() {
        if (mFragmentSwitcher.getAnimationState() == FragmentSwitcher.CLOSED || 
            mFragmentSwitcher.getAnimationState() == FragmentSwitcher.COLLAPSE_FULL || 
            mFragmentSwitcher.getAnimationState() == FragmentSwitcher.COLLAPSE) {
                mFragmentMenu.refreshNotifications();
                mFragmentSwitcher.openMenu();
        } else {
            super.onBackPressed();
        }
    }

    private int fragmentId = 0;
    private FragmentMenuListener mOnFragmentMenuListener = new FragmentMenuListener() {
        @Override
        public void onMenuClick(int buttonId) {
            switch (buttonId) {
                case R.id.btnFragmentProfile:
                    fragmentId = BaseFragment.F_PROFILE;
                    break;
                case R.id.btnFragmentDating:
                    fragmentId = BaseFragment.F_DATING;
                    break;
                case R.id.btnFragmentLikes:
                    fragmentId = BaseFragment.F_LIKES;
                    break;
                case R.id.btnFragmentMutual:
                    fragmentId = BaseFragment.F_MUTUAL;
                    break;
                case R.id.btnFragmentDialogs:
                    fragmentId = BaseFragment.F_DIALOGS;
                    break;
                case R.id.btnFragmentTops:
                    fragmentId = BaseFragment.F_TOPS;
                    break;
                case R.id.btnFragmentSettings:
                    fragmentId = BaseFragment.F_SETTINGS;
                    break; 
                default:
                    fragmentId = BaseFragment.F_PROFILE;
                    break;
            }
            mFragmentSwitcher.snapToScreen(FragmentSwitcher.EXPAND_FULL);
        }
    };
    
    private FragmentSwitchListener mFragmentSwitchListener = new FragmentSwitchListener() {
        @Override
        public void endAnimation(int Animation) {
            if (Animation == FragmentSwitcher.EXPAND_FULL) {
                mFragmentSwitcher.showFragment(fragmentId);
                mFragmentSwitcher.snapToScreen(FragmentSwitcher.COLLAPSE_FULL);
            } else {
                if (Animation == FragmentSwitcher.COLLAPSE_FULL) {
                    //mFragmentSwitcher.update();                    
                }
                if (Animation == FragmentSwitcher.COLLAPSE) {

                }
            }
        }
        
        @Override
        public void onSwitchStart() {
            mFragmentMenu.setClickable(false);
        }

        @Override
        public void onSwitchEnd() {
            mFragmentMenu.setClickable(true);
        }

        @Override
        public void onOpenStart() {
            mFragmentMenu.show();
        }
    };
}

