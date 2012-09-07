package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.FragmentContainer;
import com.topface.topface.ui.fragments.FragmentSwitchController;
import com.topface.topface.ui.fragments.FragmentMenu;
import com.topface.topface.utils.AuthorizationManager;
import com.topface.topface.utils.Debug;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
//import android.widget.Button;

public class NavigationActivity extends FragmentActivity implements View.OnClickListener {
	private int mFragmentId;
	private int mPrevFragmentId;
	private FragmentMenu mFragmentMenu;
	private FragmentContainer mFragmentContainer; // занимается переключением фрагментов
	private FragmentSwitchController mSwitchController; // занимается анимацией слоя с фрагментами
	private SharedPreferences mPreferences;
	//private Button mHomeButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_navigation);
		Debug.log(this, "onCreate");
		
		AuthorizationManager.getInstance(this).extendAccessToken();
		
		// Menu
		mFragmentMenu = new FragmentMenu();
		mFragmentMenu.setOnMenuListener(mOnMenuListener);
		getSupportFragmentManager().beginTransaction()
		        .replace(R.id.fragment_menu, mFragmentMenu).commit();
		
		// Fragments
		mFragmentContainer = new FragmentContainer(getSupportFragmentManager());

		// Switch Controller
		mSwitchController = (FragmentSwitchController) findViewById(R.id.frameAnimation);
		mSwitchController.setFragmentSwitchListener(mFragmentSwitchListener);
		mSwitchController.setFragmentMenu(mFragmentMenu);
		
        // Preferences
        mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        int lastFragmentId = mPreferences.getInt(Static.PREFERENCES_NAVIGATION_LAST_FRAGMENT, R.id.fragment_profile);

		// last opened fragment
		mFragmentContainer.showFragment(lastFragmentId/*R.id.fragment_dialogs*/, true);
		//mFragmentContainer.showFragment(/*lastFragmentId*/R.id.fragment_dialogs, true);
		
//		mHomeButton = ((Button) findViewById(R.id.btnHeaderHome));
//		mHomeButton.setOnClickListener(mOnHomeClickListener);		
	}
	
    @Override
    protected void onDestroy() {
        mPreferences.edit()
            .putInt(Static.PREFERENCES_NAVIGATION_LAST_FRAGMENT, mFragmentContainer.getCurrentFragmentId())
            .commit();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() != R.id.btnNavigationHome)
            return;
        if (mSwitchController.getAnimationState() == FragmentSwitchController.EXPAND) {
            mSwitchController.closeMenu();
        } else {
            //mFragmentMenu.refreshNotifications();
            mSwitchController.openMenu();
        }        
    }
    
    public void setSelectedMenu(int fragmentId) {
        mFragmentMenu.setSelectedMenu(fragmentId);
    }

	@Override
	public void onBackPressed() {
		if (mSwitchController.getAnimationState() == FragmentSwitchController.CLOSED
				|| mSwitchController.getAnimationState() == FragmentSwitchController.COLLAPSE_FULL
				|| mSwitchController.getAnimationState() == FragmentSwitchController.COLLAPSE) {
			mFragmentMenu.refreshNotifications();
			mSwitchController.openMenu();
		} else {
			super.onBackPressed();
		}
	}
	
    @Override
    public boolean onCreatePanelMenu(int featureId,Menu menu) {
        if (mSwitchController.getAnimationState() == FragmentSwitchController.CLOSED
                || mSwitchController.getAnimationState() == FragmentSwitchController.COLLAPSE_FULL
                || mSwitchController.getAnimationState() == FragmentSwitchController.COLLAPSE) {
            mFragmentMenu.refreshNotifications();
            mSwitchController.openMenu();
        } else if(mSwitchController.getAnimationState() == FragmentSwitchController.EXPAND) {
            mSwitchController.closeMenu();
        }
        return false;
    }

	FragmentMenu.FragmentMenuListener mOnMenuListener = new FragmentMenu.FragmentMenuListener() {
		@Override
		public void onMenuClick(int fragmentID) {
			if (mPrevFragmentId == fragmentID) {
				mSwitchController.snapToScreen(FragmentSwitchController.COLLAPSE);
			} else {
				mSwitchController.snapToScreen(FragmentSwitchController.EXPAND_FULL);
			}
			mFragmentId = fragmentID;
		}
	};

	FragmentSwitchController.FragmentSwitchListener mFragmentSwitchListener = new FragmentSwitchController.FragmentSwitchListener() {
		@Override
		public void endAnimation(int Animation) {
			if (Animation == FragmentSwitchController.EXPAND_FULL) {
				mFragmentContainer.showFragment(mFragmentId);
				mSwitchController.snapToScreen(FragmentSwitchController.COLLAPSE_FULL);
				mPrevFragmentId = mFragmentId;
			} else {
				if (Animation == FragmentSwitchController.COLLAPSE_FULL) {
					// mFragmentMenu.setVisibility(View.INVISIBLE);
					mFragmentContainer.update();					
				}
				if (Animation == FragmentSwitchController.COLLAPSE) {
					;// mFragmentMenu.setVisibility(View.INVISIBLE);
				}
			}
		}

		@Override
		public void onSwitchStart() {			
			mFragmentMenu.setClickable(false);
			//mHomeButton.setClickable(false);
		};

		@Override
		public void onSwitchEnd() {
			mFragmentMenu.setClickable(true);
			//mHomeButton.setClickable(true);
		};
		
		public void onOpenStart() {
		    mFragmentMenu.refreshNotifications();
		};
	};

}
