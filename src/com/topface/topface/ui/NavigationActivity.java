package com.topface.topface.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import com.topface.billing.BillingUtils;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.OptionsRequest;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.fragments.*;
import com.topface.topface.ui.fragments.FragmentSwitchController.FragmentSwitchListener;
import com.topface.topface.ui.fragments.MenuFragment.FragmentMenuListener;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.Calendar;

public class NavigationActivity extends BaseFragmentActivity implements View.OnClickListener {

    public static final String RATING_POPUP = "RATING_POPUP";
    public static final String FROM_AUTH = "com.topface.topface.AUTH";
    public static final int RATE_POPUP_TIMEOUT = 86400000; // 1000 * 60 * 60 * 24 * 1 (1 сутки)
    public static final int UPDATE_INTERVAL = 10 * 60 * 1000;
    private FragmentManager mFragmentManager;
    private MenuFragment mFragmentMenu;
    private FragmentSwitchController mFragmentSwitcher;

    private SharedPreferences mPreferences;
    private NoviceLayout mNoviceLayout;
    private Novice mNovice;

    private BroadcastReceiver mServerResponseReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isNeedBroughtToFront(getIntent())) {
            // При открытии активити из лаунчера перезапускаем ее
            finish();
            return;
        }
        setContentView(R.layout.ac_navigation);

        Debug.log(this, "onCreate");
        mFragmentManager = getSupportFragmentManager();

        initFragmentSwitcher();

        if (CacheProfile.isLoaded()) {
            onInit();
        }

        mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        setStopTime();
        mNovice = Novice.getInstance(mPreferences);
        mNoviceLayout = (NoviceLayout) findViewById(R.id.loNovice);
    }



    private void initFragmentSwitcher() {
        mFragmentSwitcher = (FragmentSwitchController) findViewById(R.id.fragment_switcher);
        mFragmentSwitcher.setFragmentSwitchListener(mFragmentSwitchListener);
        mFragmentSwitcher.setFragmentManager(mFragmentManager);

        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu = (MenuFragment) mFragmentManager.findFragmentById(R.id.fragment_menu);
        mFragmentMenu.setOnMenuListener(mOnFragmentMenuListener);
    }

    @Override
    public void onInit() {
        Intent intent = getIntent();
        int id = intent.getIntExtra(GCMUtils.NEXT_INTENT, -1);
        if (id != -1) {
            mFragmentSwitcher.showFragmentWithAnimation(id);

        } else {
            mFragmentSwitcher.showFragment(BaseFragment.F_DATING);
            mFragmentMenu.selectDefaultMenu();
        }
        AuthorizationManager.extendAccessToken(NavigationActivity.this);
        //Если пользователь не заполнил необходимые поля, перекидываем его на EditProfile,
        //чтобы исправлялся.
        if (needChangeProfile()) {
            Intent editIntent = new Intent(this, EditProfileActivity.class);
            editIntent.putExtra(FROM_AUTH, true);
            startActivity(editIntent);
            finish();
        } else {
            checkVersion(CacheProfile.getOptions().max_version);

        }

    }

    private boolean needChangeProfile() {
        return (CacheProfile.age == 0 || CacheProfile.city.id == 0 || CacheProfile.photo == null)
                && shouldChangeProfile();
    }

    private boolean shouldChangeProfile() {
        SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        return preferences != null && preferences.getBoolean(Static.PREFERENCES_TAG_NEED_EDIT, true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int id = intent.getIntExtra(GCMUtils.NEXT_INTENT, -1);
        if (id != -1) {
            mFragmentSwitcher.showFragmentWithAnimation(id);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkProfileUpdate();

        //Отправляем не обработанные запросы на покупку
        BillingUtils.sendQueueItems();

        mServerResponseReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(mServerResponseReceiver, new IntentFilter(OptionsRequest.VERSION_INTENT));

        //TODO костыль для ChatFragment, после перехода на фрагмент - выпилить
        if (mDelayedFragment != null) {
            onExtraFragment(mDelayedFragment);
            mDelayedFragment = null;
            mChatInvoke = true;
        }

    }

    private void checkProfileUpdate() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        long stopTime = mPreferences.getLong(Static.PREFERENCES_STOP_TIME, -1);
        if (stopTime != -1) {
            if (startTime - stopTime > UPDATE_INTERVAL) {
                App.sendProfileRequest();
            }
        }
    }

    private void checkVersion(String version) {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String curVersion = pInfo.versionName;
            if (version != null && curVersion != null) {
                String[] splittedVersion = version.split("\\.");
                String[] splittedCurVersion = curVersion.split("\\.");
                for (int i = 0; i < splittedVersion.length; i++) {
                    if (i < splittedCurVersion.length) {
                        if (Long.parseLong(splittedCurVersion[i]) < Long.parseLong(splittedVersion[i])) {
                            showOldVersionPopup();
                            return;
                        }
                    }
                }
                if (splittedCurVersion.length < splittedVersion.length) {
                    showOldVersionPopup();
                } else {
                    if (App.isOnline()) {
                        ratingPopup();
                    }
                }
            } else {
                if (App.isOnline()) {
                    ratingPopup();
                }
            }
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    private void showOldVersionPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.popup_version_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.goToMarket(NavigationActivity.this);

            }
        });
        builder.setNegativeButton(R.string.popup_version_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setMessage(R.string.general_version_not_supported);
        builder.create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setStopTime();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mServerResponseReceiver);
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
        if (mFragmentSwitcher != null) {
            if (mFragmentSwitcher.getAnimationState() == FragmentSwitchController.EXPAND) {
                super.onBackPressed();
            } else {
                if (mFragmentSwitcher.isExtraFrameShown()) {
                    //TODO костыль для ChatFragment, после перехода на фрагмент - выпилить
                    //начало костыля--------------
                    if (mChatInvoke) {
                        if (mFragmentSwitcher.getCurrentExtraFragment() instanceof ProfileFragment) {
                            ((ProfileFragment) mFragmentSwitcher.getCurrentExtraFragment()).openChat();
                            mChatInvoke = false;
                        }
                        //конец костыля--------------
                    } else {
                        mFragmentSwitcher.closeExtraFragment();
                    }
                } else {
                    mFragmentMenu.refreshNotifications();
                    mFragmentSwitcher.openMenu();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (mFragmentSwitcher != null) {
            if (mFragmentSwitcher.getAnimationState() != FragmentSwitchController.EXPAND) {
                if (mFragmentMenu != null) {
                    mFragmentMenu.refreshNotifications();
                }
                mFragmentSwitcher.openMenu();
            } else {
                mFragmentSwitcher.closeMenu();
            }
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
        Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_container);
        if (fragment instanceof DatingFragment) {
            DatingFragment datingFragment = (DatingFragment) fragment;
            datingFragment.onDialogCancel();
        }
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

            if (mNovice.isShowFillProfile()) {
                mNoviceLayout.setLayoutRes(R.layout.novice_fill_profile, mFragmentMenu.getProfileButtonOnClickListener());
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0F, 1.0F);
                alphaAnimation.setDuration(400L);
                mNoviceLayout.startAnimation(alphaAnimation);
                mNovice.completeShowFillProfile();
            }
        }

        @Override
        public void onExtraFrameOpen() {
            mFragmentMenu.unselectAllButtons();
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
        } else if (date_start == 1) {
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
                Utils.goToMarket(NavigationActivity.this);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                long stopTime = Calendar.getInstance().getTimeInMillis();
                mPreferences.edit().putLong(Static.PREFERENCES_STOP_TIME, System.currentTimeMillis()).commit();
            }
        }).start();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

    }

    @Override
    protected void onDestroy() {
        //В некоторых редких случаях выпадает NullPointerException при destroyDrawingCache,
        //поэтому на всякий случай оборачиваем в try
        try {
            super.onDestroy();
            unbindDrawables(findViewById(R.id.NavigationLayout));
            System.gc();
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        }
    }

    @Override
    public void close(Fragment fragment) {
        super.close(fragment);
        mFragmentSwitcher.showFragment(BaseFragment.F_DATING);
        mFragmentMenu.selectDefaultMenu();
    }


    @Override
    public boolean isTrackable() {
        return false;
    }

    public void onExtraFragment(final Fragment fragment) {
        mFragmentSwitcher.switchExtraFragment(fragment);
    }

    //TODO костыль для ChatFragment, после перехода на фрагмент - выпилить
    private Fragment mDelayedFragment;
    private boolean mChatInvoke = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == ContainerActivity.INTENT_CHAT_FRAGMENT) {
            if (data != null) {
                int user_id = data.getExtras().getInt(ChatFragment.INTENT_USER_ID);
                mDelayedFragment = ProfileFragment.newInstance(user_id, ProfileFragment.TYPE_USER_PROFILE);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onVipRecieved() {
        mFragmentSwitcher.showFragment(BaseFragment.F_VIP_PROFILE);
    }

}
