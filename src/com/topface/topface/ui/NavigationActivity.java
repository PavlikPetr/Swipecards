package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.FragmentSwitchController;
import com.topface.topface.ui.fragments.FragmentSwitchController.FragmentSwitchListener;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.MenuFragment.FragmentMenuListener;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.social.AuthorizationManager;

public class NavigationActivity extends FragmentActivity implements View.OnClickListener {

    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private FragmentSwitchController mFragmentSwitcher;

    public static NavigationActivity mThis = null;

    private NoviceLayout mNoviceLayout;
    private Novice mNovice;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_navigation);
        Debug.log(this, "onCreate");

        mFragmentManager = getSupportFragmentManager();

        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu.setOnMenuListener(mOnFragmentMenuListener);

        mFragmentSwitcher = (FragmentSwitchController) findViewById(R.id.fragment_switcher);
        mFragmentSwitcher.setFragmentSwitchListener(mFragmentSwitchListener);
        mFragmentSwitcher.setFragmentManager(mFragmentManager);

        Intent intent = getIntent();
        int id = intent.getIntExtra(GCMUtils.NEXT_INTENT,-1);
        if(id != -1) {
            mFragmentSwitcher.showFragmentWithAnimation(id);
        } else {
            mFragmentSwitcher.showFragment(BaseFragment.F_DATING);
            mFragmentMenu.selectDefaultMenu();
        }
        AuthorizationManager.getInstance(this).extendAccessToken();
        
        mNovice = new Novice(getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE));        
        mNoviceLayout = (NoviceLayout) findViewById(R.id.loNovice);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mThis = this;

    }

    @Override
    protected void onPause() {
        super.onPause();
        mThis = null;
    }

    /*
    *  обработчик кнопки открытия меню в заголовке фрагмента
    */
    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.btnNavigationHome)
            return;
        if (mFragmentSwitcher.getAnimationState() == FragmentSwitchController.EXPAND) {
            mFragmentSwitcher.closeMenu();
        } else {
            mFragmentSwitcher.openMenu();
        }
    }

    @Override
    public void onBackPressed() {
        if (mFragmentSwitcher.getAnimationState() == FragmentSwitchController.EXPAND) {
            super.onBackPressed();
        } else {
            mFragmentMenu.refreshNotifications();
            mFragmentSwitcher.openMenu();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (mFragmentSwitcher.getAnimationState() != FragmentSwitchController.EXPAND) {
            mFragmentMenu.refreshNotifications();
            mFragmentSwitcher.openMenu();
        } else {
            mFragmentSwitcher.closeMenu();
        }
        return false;
    }

    private FragmentMenuListener mOnFragmentMenuListener = new FragmentMenuListener() {
        @Override
        public void onMenuClick(int buttonId) {
            int fragmentId;
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
                case R.id.btnFragmentVisitors:
                    fragmentId = BaseFragment.F_VISITORS;
                    break;
                case R.id.btnFragmentSettings:
                    fragmentId = BaseFragment.F_SETTINGS;
                    break;
                default:
                    fragmentId = BaseFragment.F_PROFILE;
                    break;
            }
            mFragmentSwitcher.showFragmentWithAnimation(fragmentId);
        }
    };

    public void onDialogCancel() {
        DatingFragment datingFragment = (DatingFragment) mFragmentManager.findFragmentById(R.id.fragment_container);
        datingFragment.onDialogCancel();
    }

    private FragmentSwitchListener mFragmentSwitchListener = new FragmentSwitchListener() {
        @Override
        public void beforeExpanding() {
            mFragmentMenu.setClickable(true);
            mFragmentMenu.show();
            mFragmentMenu.refreshNotifications();
        }

        @Override
        public void afterClosing() {
            mFragmentMenu.setClickable(false);
            mFragmentMenu.hide();
        }

		@Override
		public void afterOpening() {
			if (mNovice.isMenuCompleted()) return;
			
			if (mNovice.showFillProfile) {
				mNoviceLayout.setLayoutRes(R.layout.novice_fill_profile, mFragmentMenu.getProfileButtonOnClickListener());
		        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0F, 1.0F);
		        alphaAnimation.setDuration(400L);		        
		        mNoviceLayout.startAnimation(alphaAnimation);				
				mNovice.completeShowFillProfile();
			}
		}
    };
}
