package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.fragments.feed.*;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;

public class MenuFragment extends BaseFragment implements View.OnClickListener {

    private SparseArray<Button> mButtons;

    private TextView mTvNotifyLikes;
    private TextView mTvNotifyMutual;
    private TextView mTvNotifyDialogs;
    private TextView mTvNotifyVisitors;

    private ImageViewRemote mMenuAvatar;
    private ServicesTextView mCoins;
    private ServicesTextView mLikes;
    private ImageView mProfileInfo;
    private TextView mTvNotifyFans;
    private Button buyButton;
    private boolean canChangeProfileIcons = false;
    private int mCurrentFragmentId;
    private BaseFragment mCurrentFragment;
    private boolean mHardwareAcclereated;

    public static final int DEFAULT_FRAGMENT = BaseFragment.F_DATING;
    private OnFragmentSelectedListener mOnFragmentSelected;
    private boolean mClickable = true;

    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setMenuData();
        }
    };

    private void setMenuData() {
        if (mMenuAvatar != null) {
            mMenuAvatar.setPhoto(CacheProfile.photo);
        }

        //Иконки на профиле
        if (!CacheProfile.checkIsFillData() && canChangeProfileIcons) {
            showNotEnoughDataIcon();
        } else if (CacheProfile.premium && canChangeProfileIcons) {
            showRockerIcon();
        } else {
            mProfileInfo.setVisibility(View.GONE);
        }
        canChangeProfileIcons = true;

        //Кнопка распродаж
        if (CacheProfile.getOptions().saleExists) {
            buyButton.setBackgroundResource(R.drawable.btn_sale_selector);
        } else {
            buyButton.setBackgroundResource(R.drawable.btn_blue_selector);
        }

        //Новые данные монет и лайков
        mCoins.setText(Integer.toString(CacheProfile.money));
        mLikes.setText(Integer.toString(CacheProfile.likes));
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfileUpdateReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileUpdateReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {

        View rootLayout = inflater.inflate(R.layout.fragment_menu, null);

        //Автарка в меню
        mMenuAvatar = (ImageViewRemote) rootLayout.findViewById(R.id.ivMenuAvatar);
        mMenuAvatar.setPhoto(CacheProfile.photo);
        //При клике на автарку должен происходить клик на кнопку "Профиль"
        mMenuAvatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtons.get(BaseFragment.F_PROFILE).performClick();
            }
        });

        //Делаем список кнопок
        mButtons = getButtonsMap(rootLayout);

        mCoins = (ServicesTextView) rootLayout.findViewById(R.id.menuCurCoins);
        mLikes = (ServicesTextView) rootLayout.findViewById(R.id.menuCurLikes);

        mCoins.setText(Integer.toString(CacheProfile.money));
        mLikes.setText(Integer.toString(CacheProfile.likes));

        mProfileInfo = (ImageView) rootLayout.findViewById(R.id.profileInfo);


        buyButton = (Button) rootLayout.findViewById(R.id.menuBuyBtn);
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContainerActivity.getNewIntent(ContainerActivity.INTENT_BUYING_FRAGMENT));
            }
        });


        // Notifications
        mTvNotifyLikes = (TextView) rootLayout.findViewById(R.id.tvNotifyLikes);
        mTvNotifyMutual = (TextView) rootLayout.findViewById(R.id.tvNotifyMutual);
        mTvNotifyDialogs = (TextView) rootLayout.findViewById(R.id.tvNotifyDialogs);
        mTvNotifyFans = (TextView) rootLayout.findViewById(R.id.tvNotifyFans);
        mTvNotifyVisitors = (TextView) rootLayout.findViewById(R.id.tvNotifyVisitors);

        mHardwareAcclereated = Build.VERSION.SDK_INT >= 11 && rootLayout.isHardwareAccelerated();

        return rootLayout;
    }

    private SparseArray<Button> getButtonsMap(View rootLayout) {
        SparseArray<Button> buttons = new SparseArray<Button>();
        buttons.put(BaseFragment.F_PROFILE, (Button) rootLayout.findViewById(R.id.btnFragmentProfile));
        buttons.put(BaseFragment.F_DATING, (Button) rootLayout.findViewById(R.id.btnFragmentDating));
        buttons.put(BaseFragment.F_LIKES, (Button) rootLayout.findViewById(R.id.btnFragmentLikes));
        buttons.put(BaseFragment.F_MUTUAL, (Button) rootLayout.findViewById(R.id.btnFragmentMutual));
        buttons.put(BaseFragment.F_DIALOGS, (Button) rootLayout.findViewById(R.id.btnFragmentDialogs));
        buttons.put(BaseFragment.F_FANS, (Button) rootLayout.findViewById(R.id.btnFragmentFans));
        buttons.put(BaseFragment.F_VISITORS, (Button) rootLayout.findViewById(R.id.btnFragmentVisitors));
        buttons.put(BaseFragment.F_BOOKMARKS, (Button) rootLayout.findViewById(R.id.btnFragmentBookmarks));

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
    }

    private void showNotEnoughDataIcon() {
        mProfileInfo.setImageResource(R.drawable.ic_not_enough_data);
        mProfileInfo.setVisibility(View.VISIBLE);
    }

    private void showRockerIcon() {
        mProfileInfo.setImageResource(R.drawable.ic_rocket_small);
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
            mTvNotifyLikes.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_mutual > 0) {
            mTvNotifyMutual.setText(Integer.toString(CacheProfile.unread_mutual));
            mTvNotifyMutual.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyMutual.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_messages > 0) {
            mTvNotifyDialogs.setText(Integer.toString(CacheProfile.unread_messages));
            mTvNotifyDialogs.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyDialogs.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_visitors > 0) {
            mTvNotifyVisitors.setText(Integer.toString(CacheProfile.unread_visitors));
            mTvNotifyVisitors.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyVisitors.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_fans > 0) {
            mTvNotifyFans.setText(Integer.toString(CacheProfile.unread_fans));
            mTvNotifyFans.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyFans.setVisibility(View.INVISIBLE);
        }
    }

    public void setClickable(boolean clickable) {
        mClickable = clickable;
    }

    public void selectMenu(int fragmentId) {
        Button selectedItem = mButtons.get(fragmentId);
        if (selectedItem != null) {
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
        Fragment oldFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        BaseFragment newFragment = (BaseFragment) fragmentManager.findFragmentByTag(getTagById(mCurrentFragmentId));
        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(mCurrentFragmentId);
        }

        if (oldFragment == null || newFragment != oldFragment) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //Меняем фрагменты анимировано, но только на новых устройствах c HW ускорением
            if (mHardwareAcclereated) {
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            transaction.replace(R.id.fragment_container, newFragment, getTagById(mCurrentFragmentId));
            transaction.commit();

            mCurrentFragment = newFragment;
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
        return "fragment_switch_controller_" + id;
    }

    public BaseFragment getCurrentFragment() {
        return mCurrentFragment;
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
            case BaseFragment.F_LIKES:
                fragment = new LikesFragment();
                break;
            case BaseFragment.F_MUTUAL:
                fragment = new MutualFragment();
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
            case BaseFragment.F_TOPS:
                fragment = new TopsFragment();
                break;
            case BaseFragment.F_VISITORS:
                fragment = new VisitorsFragment();
                break;
            case BaseFragment.F_SETTINGS:
                fragment = new SettingsFragment();
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

    public static interface OnFragmentSelectedListener {
        public void onFragmentSelected(int fragmentId);
    }

}