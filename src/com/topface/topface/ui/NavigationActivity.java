package com.topface.topface.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
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

import java.util.Calendar;

public class NavigationActivity extends FragmentActivity implements View.OnClickListener {

    public static final String RATING_POPUP = "RATING_POPUP";
    public static final int RATE_POPUP_TIMEOUT = 86400000; // 1000 * 60 * 60 * 24 * 1 (1 сутки)
    public static final int UPDATE_INTERVAL = 1 * 60 * 1000;
    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private FragmentSwitchController mFragmentSwitcher;

    public static NavigationActivity mThis = null;

    private SharedPreferences mPreferences;

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

        mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        setStopTime();
        mNovice = new Novice(mPreferences);
        mNoviceLayout = (NoviceLayout) findViewById(R.id.loNovice);

        if (App.isOnline()) {
            ratingPopup();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int id = intent.getIntExtra(GCMUtils.NEXT_INTENT,-1);
        if(id != -1) {
            mFragmentSwitcher.showFragmentWithAnimation(id);
        } else {
            mFragmentSwitcher.showFragment(BaseFragment.F_DATING);
            mFragmentMenu.selectDefaultMenu();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mThis = this;
        long startTime = Calendar.getInstance().getTimeInMillis();
        long stopTime = mPreferences.getLong(Static.PREFERENCES_STOP_TIME,-1);
        if(stopTime != -1) {
            if(startTime - stopTime > UPDATE_INTERVAL) {
                ProfileRequest pr = new ProfileRequest(this);
                pr.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) throws NullPointerException {}

                    @Override
                    public void fail(int codeError, ApiResponse response) throws NullPointerException {}
                }).exec();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mThis = null;
        setStopTime();
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


    /**
     * Попап с предложение оценить предложение
     */
    private void ratingPopup() {
        final SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        long date_start = preferences.getLong(RATING_POPUP, 1);
        long date_now = new java.util.Date().getTime();

        if (date_start == 0 || (date_now - date_start < RATE_POPUP_TIMEOUT)) {
            return;
        }
        else if (date_start == 1) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("RATING_POPUP", new java.util.Date().getTime());
            editor.commit();
            return;
        }

        final Dialog ratingPopup = new Dialog(this) {
            @Override
            public void onBackPressed() {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, new java.util.Date().getTime());
                editor.commit();
                super.onBackPressed();
            }
        };
        ratingPopup.setTitle(R.string.dashbrd_popup_title);
        ratingPopup.setContentView(R.layout.popup_rating);
        ratingPopup.show();

        ratingPopup.findViewById(R.id.btnRatingPopupRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.topface.topface")));
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, 0);
                editor.commit();
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupLate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, new java.util.Date().getTime());
                editor.commit();
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(RATING_POPUP, 0);
                editor.commit();
                ratingPopup.cancel();
            }
        });
    }

    private void setStopTime() {
        long stopTime = Calendar.getInstance().getTimeInMillis();
        mPreferences.edit().putLong(Static.PREFERENCES_STOP_TIME,stopTime).commit();
    }
}
