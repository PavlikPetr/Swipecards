package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;

public class MenuFragment extends Fragment implements View.OnClickListener {
    private View mRootLayout;

    private Button[] mButtons;

    private TextView mTvNotifyLikes;
    private TextView mTvNotifyMutual;
    private TextView mTvNotifyDialogs;
    private TextView mTvNotifyVisitors;

    private FragmentMenuListener mFragmentMenuListener;
    private Button mDefaultMenuItem;
    BroadcastReceiver mBroadcastReceiver;
    private ImageViewRemote mMenuAvatar;
    private BroadcastReceiver mProfileUpdateReceiver;

    public interface FragmentMenuListener {
        public void onMenuClick(int buttonId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfileUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(mMenuAvatar != null) {
                    mMenuAvatar.setPhoto(CacheProfile.photo);
                }
            }
        };
    }

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
        if (mRootLayout != null)
            return mRootLayout;

        mRootLayout = inflater.inflate(R.layout.fragment_menu, null);


        // Buttons
        final Button btnProfile = (Button) mRootLayout.findViewById(R.id.btnFragmentProfile);
        btnProfile.setOnClickListener(this);
        mMenuAvatar = (ImageViewRemote) mRootLayout.findViewById(R.id.ivMenuAvatar);
        mMenuAvatar.setPhoto(CacheProfile.photo);
        mMenuAvatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                btnProfile.performClick();
            }
        });

        mDefaultMenuItem = (Button) mRootLayout.findViewById(R.id.btnFragmentDating);

        mButtons = new Button[]{
                btnProfile,
                mDefaultMenuItem,
                (Button) mRootLayout.findViewById(R.id.btnFragmentLikes),
                (Button) mRootLayout.findViewById(R.id.btnFragmentMutual),
                (Button) mRootLayout.findViewById(R.id.btnFragmentDialogs),
                (Button) mRootLayout.findViewById(R.id.btnFragmentTops),
                (Button) mRootLayout.findViewById(R.id.btnFragmentVisitors),
                (Button) mRootLayout.findViewById(R.id.btnFragmentSettings),
                (Button) mRootLayout.findViewById(R.id.btnFragmentOfferwall)
        };

        for (Button btn : mButtons) {
            btn.setOnClickListener(this);
        }

        // Notifications
        mTvNotifyLikes = (TextView) mRootLayout.findViewById(R.id.tvNotifyLikes);
        mTvNotifyMutual = (TextView) mRootLayout.findViewById(R.id.tvNotifyMutual);
        mTvNotifyDialogs = (TextView) mRootLayout.findViewById(R.id.tvNotifyDialogs);
        mTvNotifyVisitors = (TextView) mRootLayout.findViewById(R.id.tvNotifyVisitors);

        hide();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshNotifications();
            }
        };

        return mRootLayout;
    }



    @Override
    public void onClick(View view) {
        unselectAllButtons();

        view.setSelected(true);

        if (mFragmentMenuListener != null)
            mFragmentMenuListener.onMenuClick(view.getId());
    }

    public void unselectAllButtons() {
        for (Button btn : mButtons)
            btn.setSelected(false);
    }

    public void setOnMenuListener(FragmentMenuListener onFragmentMenuListener) {
        mFragmentMenuListener = onFragmentMenuListener;
    }

    public void refreshNotifications() {
        if (CacheProfile.unread_likes > 0) {
            mTvNotifyLikes.setText(" " + CacheProfile.unread_likes + " ");
            mTvNotifyLikes.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyLikes.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_mutual > 0) {
            mTvNotifyMutual.setText(" " + CacheProfile.unread_mutual + " ");
            mTvNotifyMutual.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyMutual.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_messages > 0) {
            mTvNotifyDialogs.setText(" " + CacheProfile.unread_messages + " ");
            mTvNotifyDialogs.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyDialogs.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_visitors > 0) {
            mTvNotifyVisitors.setText(" " + CacheProfile.unread_visitors + " ");
            mTvNotifyVisitors.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyVisitors.setVisibility(View.INVISIBLE);
        }
    }

    public void show() {
        mRootLayout.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mRootLayout.setVisibility(View.INVISIBLE);
    }

    public void setClickable(boolean clickable) {
        for (Button btn : mButtons) {
            btn.setClickable(clickable);
        }
    }

    public void selectDefaultMenu() {
        mDefaultMenuItem.setSelected(true);
    }

    public OnClickListener getProfileButtonOnClickListener() {
        final Button btnProfile = (Button) mRootLayout.findViewById(R.id.btnFragmentProfile);
        return new OnClickListener() {

            @Override
            public void onClick(View v) {
                btnProfile.performClick();
            }
        };
    }
}




