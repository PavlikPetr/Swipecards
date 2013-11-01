package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.ClosingsBuyVipDialog;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;
import com.topface.topface.ui.fragments.closing.MutualClosingFragment;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.http.ProfileBackgrounds;

public class MenuFragment extends BaseFragment implements View.OnClickListener {

    public static final String SELECT_MENU_ITEM = "com.topface.topface.action.menu.selectitem";
    public static final String SELECTED_FRAGMENT_ID = "com.topface.topface.action.menu.item";
    public static boolean logoutInvoked = false;
    private SparseArray<Button> mButtons;

    private TextView mTvNotifyLikes;
    private TextView mTvNotifyMutual;
    private TextView mTvNotifyDialogs;
    private TextView mTvNotifyVisitors;
    private TextView mTvNotifyAdmirations;

    private ImageViewRemote mMenuAvatar;
    private ServicesTextView mCoins;
    private ServicesTextView mLikes;
    private ImageView mProfileInfo;
    private TextView mTvNotifyFans;
    private Button buyButton;
    private boolean canChangeProfileIcons = false;
    private int mCurrentFragmentId;
    private boolean mHardwareAccelerated;

    public static final int DEFAULT_FRAGMENT = BaseFragment.F_DATING;
    private OnFragmentSelectedListener mOnFragmentSelected;
    private boolean mClickable = true;

    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setMenuData();
            switchProfileButton();
        }
    };

    private BroadcastReceiver mProductsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Кнопка распродаж
            if (CacheProfile.getGooglePlayProducts().saleExists) {
                buyButton.setBackgroundResource(R.drawable.btn_sale_selector);
            } else {
                buyButton.setBackgroundResource(R.drawable.btn_blue_selector);
            }
        }
    };

    private boolean isClosed = false;

    private BroadcastReceiver mSelectMenuReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int fragmentId = intent.getExtras().getInt(SELECTED_FRAGMENT_ID);
            selectMenu(fragmentId);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(false);
    }

    private void setMenuData() {
        if (mMenuAvatar != null) {
            mMenuAvatar.setPhoto(CacheProfile.photo);
        }
        //Иконки на профиле
        if (!CacheProfile.checkIsFillData() && canChangeProfileIcons) {
            showNotEnoughDataIcon();
        } else {
            mProfileInfo.setVisibility(View.GONE);
        }
        canChangeProfileIcons = true;
        buyButton.setBackgroundResource(R.drawable.btn_blue_selector);
        //Новые данные монет и лайков
        mCoins.setText(Integer.toString(CacheProfile.money));
        mLikes.setText(Integer.toString(CacheProfile.likes));
    }

    private BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshNotifications();
            //Новые данные монет и лайков
            mCoins.setText(Integer.toString(CacheProfile.money));
            mLikes.setText(Integer.toString(CacheProfile.likes));
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        refreshNotifications();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfileUpdateReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProductsUpdateReceiver, new IntentFilter(GooglePlayProducts.INTENT_UPDATE_PRODUCTS));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_BALANCE_COUNTERS));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSelectMenuReceiver, new IntentFilter(SELECT_MENU_ITEM));
        switchProfileButton();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileUpdateReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProductsUpdateReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mCountersReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSelectMenuReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View rootLayout = inflater.inflate(R.layout.fragment_menu, null);

        View profileLayout = getProfileLayout(rootLayout);
        //Делаем список кнопок
        mButtons = getButtonsMap(rootLayout, profileLayout);

        //Автарка в меню
        initMenuAvatar(profileLayout);
        mProfileInfo = (ImageView) profileLayout.findViewById(R.id.profileInfo);

        mCoins = (ServicesTextView) rootLayout.findViewById(R.id.menuCurCoins);
        mLikes = (ServicesTextView) rootLayout.findViewById(R.id.menuCurLikes);
        mCoins.setText(Integer.toString(CacheProfile.money));
        mLikes.setText(Integer.toString(CacheProfile.likes));

        buyButton = (Button) rootLayout.findViewById(R.id.menuBuyBtn);
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContainerActivity.getBuyingIntent("Menu"));
            }
        });

        // Notifications
        mTvNotifyLikes = (TextView) rootLayout.findViewById(R.id.tvNotifyLikes);
        mTvNotifyMutual = (TextView) rootLayout.findViewById(R.id.tvNotifyMutual);
        mTvNotifyDialogs = (TextView) rootLayout.findViewById(R.id.tvNotifyDialogs);
        mTvNotifyFans = (TextView) rootLayout.findViewById(R.id.tvNotifyFans);
        mTvNotifyVisitors = (TextView) rootLayout.findViewById(R.id.tvNotifyVisitors);
        mTvNotifyAdmirations = (TextView) rootLayout.findViewById(R.id.tvNotifyAdmirations);
        mHardwareAccelerated = isHardwareAccelerated(rootLayout);

        return rootLayout;
    }

    private void initMenuAvatar(View profileLayout) {
        mMenuAvatar = (ImageViewRemote) profileLayout.findViewById(R.id.ivMenuAvatar);
        mMenuAvatar.setPhoto(CacheProfile.photo);
        //При клике на автарку должен происходить клик на кнопку "Профиль"
        mMenuAvatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtons.get(BaseFragment.F_PROFILE).performClick();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean isHardwareAccelerated(View rootLayout) {
        return Build.VERSION.SDK_INT >= 11 && rootLayout.isHardwareAccelerated();
    }

    private SparseArray<Button> getButtonsMap(View rootLayout, View profileLayout) {
        SparseArray<Button> buttons = new SparseArray<Button>();
        buttons.put(BaseFragment.F_PROFILE, (Button) profileLayout.findViewById(R.id.btnFragmentProfile));
        buttons.put(BaseFragment.F_DATING, (Button) rootLayout.findViewById(R.id.btnFragmentDating));
        buttons.put(BaseFragment.F_ADMIRATIONS, (Button) rootLayout.findViewById(R.id.btnFragmentAdmirations));
        buttons.put(BaseFragment.F_LIKES, (Button) rootLayout.findViewById(R.id.btnFragmentLikes));
        buttons.put(BaseFragment.F_MUTUAL, (Button) rootLayout.findViewById(R.id.btnFragmentMutual));
        buttons.put(BaseFragment.F_DIALOGS, (Button) rootLayout.findViewById(R.id.btnFragmentDialogs));
        buttons.put(BaseFragment.F_FANS, (Button) rootLayout.findViewById(R.id.btnFragmentFans));
        buttons.put(BaseFragment.F_VISITORS, (Button) rootLayout.findViewById(R.id.btnFragmentVisitors));
        buttons.put(BaseFragment.F_BOOKMARKS, (Button) rootLayout.findViewById(R.id.btnFragmentBookmarks));
        buttons.put(BaseFragment.F_EDITOR, (Button) rootLayout.findViewById(R.id.btnEditor));

        //Устанавливаем теги и листенеры на кнопки
        for (int i = 0; i < buttons.size(); i++) {
            int key = buttons.keyAt(i);
            Button button = buttons.get(key);
            if (button != null) {
                button.setOnClickListener(this);
                button.setTag(key);
            }
        }

        return buttons;
    }

    public void onLoadProfile() {
        setMenuData();
        if (Editor.isEditor()) {
            mButtons.get(F_EDITOR).setVisibility(View.VISIBLE);
        }
    }

    private void showNotEnoughDataIcon() {
        mProfileInfo.setImageResource(R.drawable.ic_not_enough_data);
        mProfileInfo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        if (mClickable) {
            unselectAllButtons();
            view.setSelected(true);
            showFragment((Integer) view.getTag());
        }
    }

    public void unselectAllButtons() {
        for (int i = 0; i < mButtons.size(); i++) {
            mButtons.get(mButtons.keyAt(i)).setSelected(false);
        }
    }

    public void refreshNotifications() {
        if (CacheProfile.unread_likes > 0) {
            mTvNotifyLikes.setText(Integer.toString(CacheProfile.unread_likes));
            mTvNotifyLikes.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyLikes.setVisibility(View.GONE);
        }

        if (CacheProfile.unread_mutual > 0) {
            mTvNotifyMutual.setText(Integer.toString(CacheProfile.unread_mutual));
            mTvNotifyMutual.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyMutual.setVisibility(View.GONE);
        }

        if (CacheProfile.unread_messages > 0) {
            mTvNotifyDialogs.setText(Integer.toString(CacheProfile.unread_messages));
            mTvNotifyDialogs.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyDialogs.setVisibility(View.GONE);
        }

        if (CacheProfile.unread_visitors > 0) {
            mTvNotifyVisitors.setText(Integer.toString(CacheProfile.unread_visitors));
            mTvNotifyVisitors.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyVisitors.setVisibility(View.GONE);
        }

        if (CacheProfile.unread_fans > 0) {
            mTvNotifyFans.setText(Integer.toString(CacheProfile.unread_fans));
            mTvNotifyFans.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyFans.setVisibility(View.GONE);
        }

        if (CacheProfile.unread_admirations > 0) {
            mTvNotifyAdmirations.setText(Integer.toString(CacheProfile.unread_admirations));
            mTvNotifyAdmirations.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyAdmirations.setVisibility(View.GONE);
        }
    }

    public void setClickable(boolean clickable) {
        mClickable = clickable;
    }

    public void selectMenu(int fragmentId) {
        Button selectedItem = mButtons.get(fragmentId);
        if (selectedItem != null) {
            unselectAllButtons();
            selectedItem.setSelected(true);
            showFragment(fragmentId);
        }
    }

    public OnClickListener getProfileButtonOnClickListener() {
        final Button btnProfile = (Button) getView().findViewById(R.id.btnFragmentProfile);
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnProfile.performClick();
            }
        };
    }

    public void showFragment(int fragmentId) {
        if (fragmentId != mCurrentFragmentId) {
            mCurrentFragmentId = fragmentId;
            switchFragment();
        } else if (mOnFragmentSelected != null) {
            mOnFragmentSelected.onFragmentSelected(fragmentId);
        }
    }

    private void switchFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment oldFragment = fragmentManager.findFragmentById(R.id.fragment_content);

        String fragmentTag = getTagById(mCurrentFragmentId);
        BaseFragment newFragment = (BaseFragment) fragmentManager.findFragmentByTag(fragmentTag);
        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(mCurrentFragmentId);
        }

        if (oldFragment == null || newFragment != oldFragment) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //Меняем фрагменты анимировано, но только на новых устройствах c HW ускорением
            if (mHardwareAccelerated) {
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            if (!newFragment.isAdded()) {
                if (oldFragment != null) {
                    transaction.remove(oldFragment);
                }
                transaction.add(R.id.fragment_content, newFragment, fragmentTag);
                transaction.commitAllowingStateLoss();
            } else {
                transaction.show(newFragment);
            }
        }

        //Закрываем меню только после создания фрагмента
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnFragmentSelected.onFragmentSelected(mCurrentFragmentId);
            }
        }, 250);

    }

    private String getTagById(int id) {
        if (id == F_LIKES && !LikesClosingFragment.usersProcessed) {
            return "fragment_switch_controller_closed_" + id;
        } else if (id == F_MUTUAL && !MutualClosingFragment.usersProcessed) {
            return "fragment_switch_controller_closed_" + id;
        } else {
            return "fragment_switch_controller_" + id;
        }
    }

    private BaseFragment getFragmentNewInstanceById(int id) {
        BaseFragment fragment;
        switch (id) {
            case BaseFragment.F_VIP_PROFILE:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE,
                        VipBuyFragment.class.getName());
                break;
            case BaseFragment.F_PROFILE:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE);
                break;
            case BaseFragment.F_DATING:
                fragment = new DatingFragment();
                break;
            case BaseFragment.F_ADMIRATIONS:
                fragment = new AdmirationFragment();
                break;
            case BaseFragment.F_LIKES:
                if (LikesClosingFragment.usersProcessed || CacheProfile.premium) {
                    fragment = new LikesFragment();
                } else {
                    if (!isClosed)
                        getActivity().getIntent().putExtra(GCMUtils.NEXT_INTENT, BaseFragment.F_LIKES);
                    Debug.log("Closing:Last fragment F_LIKES from MenuFragment");
                    fragment = new LikesClosingFragment();
                }
                break;
            case BaseFragment.F_MUTUAL:
                fragment = MutualClosingFragment.usersProcessed || CacheProfile.premium ?
                        new MutualFragment() : new MutualClosingFragment();
                break;
            case BaseFragment.F_DIALOGS:
                fragment = new DialogsFragment();
                break;
            case BaseFragment.F_BOOKMARKS:
                fragment = new BookmarksFragment();
                break;
            case BaseFragment.F_FANS:
                fragment = new FansFragment();
                break;
            case BaseFragment.F_VISITORS:
                fragment = new VisitorsFragment();
                break;
            case BaseFragment.F_SETTINGS:
                fragment = new SettingsFragment();
                break;
            case BaseFragment.F_EDITOR:
                fragment = null;
                if (Editor.isEditor()) {
                    fragment = new EditorFragment();
                }
                break;
            default:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE);
                break;
        }
        return fragment;
    }

    public int getCurrentFragmentId() {
        return mCurrentFragmentId == 0 ? BaseFragment.F_DATING : mCurrentFragmentId;
    }

    public void setOnFragmentSelected(OnFragmentSelectedListener listener) {
        mOnFragmentSelected = listener;
    }

    public static void onLogout() {
        logoutInvoked = true;
    }

    public View getProfileLayout(View rootLayout) {
        ViewGroup profileLayout = (ViewGroup) rootLayout.findViewById(R.id.btnProfileLayout);
        if (!needProfileBackground()) return profileLayout;
        boolean switchLayoutToPremium = false;
        if (CacheProfile.premium) {
            if (ProfileBackgrounds.isVipBackgroundId(getActivity(), CacheProfile.background_id)) {
                profileLayout.setVisibility(View.GONE);
                profileLayout = (ViewGroup) rootLayout.findViewById(R.id.btnProfileLayoutWithBackground);
                profileLayout.setVisibility(View.VISIBLE);
                String name = CacheProfile.first_name.length() <= 1
                        ? getString(R.string.general_profile) : CacheProfile.first_name;
                ((Button) profileLayout.findViewById(R.id.btnUserName)).setText(name);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                        ProfileBackgrounds.getBackgroundResource(getActivity(), CacheProfile.background_id));
                ((ImageViewRemote) profileLayout.findViewById(R.id.ivProfileBackground))
                        .setRemoteImageBitmap(bitmap);
                switchLayoutToPremium = true;
            }
        }
        if (!switchLayoutToPremium) {
            rootLayout.findViewById(R.id.btnProfileLayoutWithBackground).setVisibility(View.GONE);
            profileLayout.setVisibility(View.VISIBLE);
        }
        rootLayout.requestLayout();
        rootLayout.invalidate();
        return profileLayout;
    }

    public static interface OnFragmentSelectedListener {
        public void onFragmentSelected(int fragmentId);
    }

    public void showNovice(Novice novice) {
        if (novice != null && novice.isFlagsInitializationProccesed()) {
            if (novice.isMenuCompleted()) return;

            if (novice.isShowFillProfile()) {
                RelativeLayout rootLayout = (RelativeLayout) getView().findViewById(R.id.MenuLayout);
                NoviceLayout noviceLayout = new NoviceLayout(getActivity());
                noviceLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                rootLayout.addView(noviceLayout);
                noviceLayout.setLayoutRes(
                        R.layout.novice_fill_profile,
                        this.getProfileButtonOnClickListener()
                );
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0F, 1.0F);
                alphaAnimation.setDuration(400L);
                noviceLayout.startAnimation(alphaAnimation);
                novice.completeShowFillProfile();
            }
        }
    }

    public void onClosings(int type) {
        for (int i = 0; i < mButtons.size(); i++) {
            int key = mButtons.keyAt(i);
            Button btn = mButtons.get(key);
            if (key != F_PROFILE && key != F_EDITOR && key != type) {
                setAlphaToTextAndDrawable(btn, 102);
                btn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mCurrentFragmentId == F_MUTUAL) {
                            showWatchAsListDialog(CacheProfile.unread_mutual);
                        } else if (mCurrentFragmentId == F_LIKES) {
                            showWatchAsListDialog(CacheProfile.unread_likes);
                        } else {
                            showWatchAsListDialog(CacheProfile.unread_likes + CacheProfile.unread_mutual);
                        }
                    }
                });
            } else {
                setAlphaToTextAndDrawable(btn, 255);
                btn.setOnClickListener(this);
            }
        }
        isClosed = true;
    }

    private void setAlphaToTextAndDrawable(Button btn, int alpha) {
        btn.setTextColor(Color.argb(alpha, 255, 255, 255));
        if (btn.getCompoundDrawables()[0] != null) {
            btn.getCompoundDrawables()[0].setAlpha(alpha);
        }
    }

    public void onStopClosings() {
        for (int i = 0; i < mButtons.size(); i++) {
            Button btn = mButtons.get(mButtons.keyAt(i));
            setAlphaToTextAndDrawable(btn, 255);
            btn.setOnClickListener(this);
        }
        mCurrentFragmentId = F_UNDEFINED;
        isClosed = false;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void showWatchAsListDialog(int likesCount) {
        if (ClosingsBuyVipDialog.opened) return;

        ClosingsBuyVipDialog newFragment = ClosingsBuyVipDialog.newInstance(likesCount);
        newFragment.setOnWatchSequentialyListener(new ClosingsBuyVipDialog.IWatchSequentialyListener() {
            @Override
            public void onWatchSequentialy() {
                Activity activity = getActivity();
                if (activity instanceof NavigationActivity) {
                    ((NavigationActivity) activity).hideContent();
                }
            }
        });
        newFragment.setOnWatchListListener(new ClosingsBuyVipDialog.IWatchListListener() {

            @Override
            public void onWatchList() {
                Activity activity = getActivity();
                if (activity instanceof NavigationActivity) {
                    ((NavigationActivity) activity).hideContent();
                }
            }
        });
        try {
            newFragment.show(getActivity().getSupportFragmentManager(), ClosingsBuyVipDialog.TAG);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    @Override
    protected boolean needOptionsMenu() {
        return false;
    }

    public void switchProfileButton() {
        View profileLayout = getProfileLayout(getView());
        initMenuAvatar(profileLayout);
        Button profileButton = (Button) profileLayout.findViewById(R.id.btnFragmentProfile);
        final Button hashedProfileButton = mButtons.get(BaseFragment.F_PROFILE);
        if (profileButton != hashedProfileButton) {
            profileButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hashedProfileButton.performClick();
                }
            });
        }
    }

    private boolean needProfileBackground() {
        return getResources().getBoolean(R.bool.needProfileBackground);
    }
}