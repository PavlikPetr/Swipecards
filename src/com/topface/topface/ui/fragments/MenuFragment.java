package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;

public class MenuFragment extends Fragment implements View.OnClickListener {
    private View mRootLayout;

    private Button mBtnProfile;
    private Button mBtnDating;
    private Button mBtnLikes;
    private Button mBtnMutual;
    private Button mBtnDialogs;
    private Button mBtnTops;
    private Button mBtnSettings;
    private Button[] mButtons;

    private TextView mTvNotifyLikes;
    private TextView mTvNotifyMutual;
    private TextView mTvNotifyDialogs;

    private FragmentMenuListener mFragmentMenuListener;

    public interface FragmentMenuListener {
        public void onMenuClick(int buttonId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        if (mRootLayout != null)
            return mRootLayout;

        mRootLayout = inflater.inflate(R.layout.fragment_menu, null);


        // Buttons
        mBtnProfile = (Button) mRootLayout.findViewById(R.id.btnFragmentProfile);
        mBtnProfile.setOnClickListener(this);
        ImageViewRemote menuAvatar = (ImageViewRemote) mRootLayout.findViewById(R.id.ivMenuAvatar);
        menuAvatar.setPhoto(CacheProfile.photo);

        mBtnDating = (Button) mRootLayout.findViewById(R.id.btnFragmentDating);
        mBtnDating.setOnClickListener(this);

        mBtnLikes = (Button) mRootLayout.findViewById(R.id.btnFragmentLikes);
        mBtnLikes.setOnClickListener(this);

        mBtnMutual = (Button) mRootLayout.findViewById(R.id.btnFragmentMutual);
        mBtnMutual.setOnClickListener(this);

        mBtnDialogs = (Button) mRootLayout.findViewById(R.id.btnFragmentDialogs);
        mBtnDialogs.setOnClickListener(this);

        mBtnTops = (Button) mRootLayout.findViewById(R.id.btnFragmentTops);
        mBtnTops.setOnClickListener(this);

        mBtnSettings = (Button) mRootLayout.findViewById(R.id.btnFragmentSettings);
        mBtnSettings.setOnClickListener(this);

        mButtons = new Button[]{mBtnProfile, mBtnDating, mBtnLikes, mBtnMutual, mBtnDialogs, mBtnTops, mBtnSettings};

        // Notifications
        mTvNotifyLikes = (TextView) mRootLayout.findViewById(R.id.tvNotifyLikes);
        mTvNotifyMutual = (TextView) mRootLayout.findViewById(R.id.tvNotifyMutual);
        mTvNotifyDialogs = (TextView) mRootLayout.findViewById(R.id.tvNotifyDialogs);

        hide();

        return mRootLayout;
    }

    @Override
    public void onClick(View view) {
        for (Button btn : mButtons)
            btn.setSelected(false);

        view.setSelected(true);

        if (mFragmentMenuListener != null)
            mFragmentMenuListener.onMenuClick(view.getId());
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
    }

    public void show() {
        mRootLayout.setVisibility(View.VISIBLE);
    }

    public void hide() {
        mRootLayout.setVisibility(View.INVISIBLE);
    }

    public void setClickable(boolean clickable) {
        mBtnProfile.setClickable(clickable);
        mBtnDating.setClickable(clickable);
        mBtnLikes.setClickable(clickable);
        mBtnMutual.setClickable(clickable);
        mBtnDialogs.setClickable(clickable);
        mBtnTops.setClickable(clickable);
        mBtnSettings.setClickable(clickable);
    }

    public void setSelectedMenu(int fragmentId) {
        switch (fragmentId) {
            case BaseFragment.F_PROFILE:
                mBtnProfile.setSelected(true);
                break;
            case BaseFragment.F_DATING:
                mBtnDating.setSelected(true);
                break;
            case BaseFragment.F_LIKES:
                mBtnLikes.setSelected(true);
                break;
            case BaseFragment.F_MUTUAL:
                mBtnMutual.setSelected(true);
                break;
            case BaseFragment.F_DIALOGS:
                mBtnDialogs.setSelected(true);
                break;
            case BaseFragment.F_TOPS:
                mBtnTops.setSelected(true);
                break;
            case BaseFragment.F_SETTINGS:
                mBtnSettings.setSelected(true);
                break;
            default:
                mBtnProfile.setSelected(true);
                break;
        }
    }

}




