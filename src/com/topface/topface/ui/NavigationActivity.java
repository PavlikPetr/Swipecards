package com.topface.topface.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.FragmentSwitchController;
import com.topface.topface.ui.fragments.FragmentSwitchController.FragmentSwitchListener;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.MenuFragment.FragmentMenuListener;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.topface.topface.utils.social.AuthorizationManager;

public class NavigationActivity extends FragmentActivity implements View.OnClickListener {

    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private FragmentSwitchController mFragmentSwitcher;
    private SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_navigation);
        Debug.log(this, "onCreate");

        AuthorizationManager.getInstance(this).extendAccessToken();

        mFragmentManager = getSupportFragmentManager();

        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu.setOnMenuListener(mOnFragmentMenuListener);

        mFragmentSwitcher = (FragmentSwitchController) findViewById(R.id.fragment_switcher);
        mFragmentSwitcher.setFragmentSwitchListener(mFragmentSwitchListener);
        mFragmentSwitcher.setFragmentManager(mFragmentManager);

        mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        int lastFragmentId = mPreferences.getInt(Static.PREFERENCES_NAVIGATION_LAST_FRAGMENT, BaseFragment.F_PROFILE);
        CacheProfile.background_id = mPreferences.getInt(Static.PREFERENCES_PROFILE_BACKGROUND_ID, ProfileBackgrounds.DEFAULT_BACKGROUND_ID);

        mFragmentSwitcher.showFragment(lastFragmentId);
        mFragmentMenu.setSelectedMenu(lastFragmentId);
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

    @Override
    protected void onDestroy() {
        mPreferences.edit()
                .putInt(Static.PREFERENCES_NAVIGATION_LAST_FRAGMENT, mFragmentSwitcher.getCurrentFragmentId())
                .commit();
        super.onDestroy();
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
    };
}

