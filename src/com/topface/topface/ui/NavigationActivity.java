package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.DialogsFragment;
import com.topface.topface.ui.fragments.FragmentMenu;
import com.topface.topface.ui.fragments.FragmentMenu.FragmentMenuListener;
import com.topface.topface.ui.fragments.FragmentSwitcher;
import com.topface.topface.ui.fragments.LikesFragment;
import com.topface.topface.ui.fragments.MutualFragment;
import com.topface.topface.ui.fragments.ProfileFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.utils.AuthorizationManager;
import com.topface.topface.utils.Debug;

public class NavigationActivity extends FragmentActivity implements View.OnClickListener {

    private FragmentMenu mFragmentMenu;
    private FragmentSwitcher mFragmentSwitcher;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_navigation);
		Debug.log(this, "onCreate");
		
		AuthorizationManager.getInstance(this).extendAccessToken();
		
		// Fragment Menu
		mFragmentMenu = (FragmentMenu)getSupportFragmentManager().findFragmentById(R.id.fragment_menu);
		mFragmentMenu.setOnMenuListener(mOnFragmentMenuListener);

        // Fragment Switcher
	    mFragmentSwitcher = (FragmentSwitcher)findViewById(R.id.fragment_switcher);
	    mFragmentSwitcher.setFragmentMenu(mFragmentMenu);
	    mFragmentSwitcher.setFragmentManager(getSupportFragmentManager());
	    mFragmentSwitcher.showFragment(0);
	}
	
    public void setSelectedMenu(int fragmentId) {
        mFragmentMenu.setSelectedMenu(fragmentId);
    }

    @Override
    public void onClick(View v) {
        mFragmentSwitcher.openMenu();
    }
    
    FragmentMenuListener mOnFragmentMenuListener = new FragmentMenuListener() {
        @Override
        public void onMenuClick(int buttonId) {
            mFragmentSwitcher.closeMenu();
            
            BaseFragment baseFragment = null;
            switch (buttonId) {
                case R.id.btnFragmentProfile:
                    baseFragment = new ProfileFragment();
                    break;
                case R.id.btnFragmentDating:
                    baseFragment = new DatingFragment();
                    break;
                case R.id.btnFragmentLikes:
                    baseFragment = new LikesFragment();
                    break;
                case R.id.btnFragmentMutual:
                    baseFragment = new MutualFragment();
                    break;
                case R.id.btnFragmentDialogs:
                    baseFragment = new DialogsFragment();
                    break;
                case R.id.btnFragmentTops:
                    baseFragment = new TopsFragment();
                    break;
                case R.id.btnFragmentSettings:
                    baseFragment = new SettingsFragment();
                    break; 
                default:
                    baseFragment = new ProfileFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentFragment, baseFragment).commit();
        }
    };
}

