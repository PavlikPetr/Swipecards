package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class FragmentMenu extends Fragment implements View.OnClickListener {
    private View mRootLayout;
    private FragmentMenuListener mFragmentMenuListener;
    
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
    
    public interface FragmentMenuListener {
        public void onMenuClick(int fragmentID);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        if(mRootLayout != null)
            return mRootLayout;
        
        mRootLayout = inflater.inflate(R.layout.fragment_menu, null);
        
        mBtnProfile = (Button)mRootLayout.findViewById(R.id.btnFragmentProfile);
        mBtnProfile.setOnClickListener(this);
        
        mBtnDating = (Button)mRootLayout.findViewById(R.id.btnFragmentDating);
        mBtnDating.setOnClickListener(this);
        
        mBtnLikes = (Button)mRootLayout.findViewById(R.id.btnFragmentLikes);
        mBtnLikes.setOnClickListener(this);
        
        mBtnMutual = (Button)mRootLayout.findViewById(R.id.btnFragmentMutual);
        mBtnMutual.setOnClickListener(this);
        
        mBtnDialogs = (Button)mRootLayout.findViewById(R.id.btnFragmentDialogs);
        mBtnDialogs.setOnClickListener(this);
        
        mBtnTops = (Button)mRootLayout.findViewById(R.id.btnFragmentTops);
        mBtnTops.setOnClickListener(this);
        
        mBtnSettings = (Button)mRootLayout.findViewById(R.id.btnFragmentSettings);
        mBtnSettings.setOnClickListener(this);
        
        //mRootLayout.setVisibility(View.INVISIBLE);
        
        mButtons = new Button[]{mBtnProfile, mBtnDating, mBtnLikes, mBtnMutual, mBtnDialogs, mBtnTops, mBtnSettings};
        
        
        mTvNotifyLikes = (TextView)mRootLayout.findViewById(R.id.tvNotifyLikes);
        mTvNotifyMutual = (TextView)mRootLayout.findViewById(R.id.tvNotifyMutual);
        mTvNotifyDialogs = (TextView)mRootLayout.findViewById(R.id.tvNotifyDialogs);

        return mRootLayout;
    }
    
    @Override
    public void onClick(View view) {
        int fragmentId = 0;
        
        for (Button btn : mButtons)
            btn.setSelected(false);
        
        switch (view.getId()) {
            case R.id.btnFragmentProfile:
                fragmentId = R.id.fragment_profile;
                mBtnProfile.setSelected(true);
                break;
            case R.id.btnFragmentDating:
                fragmentId = R.id.fragment_dating;
                mBtnDating.setSelected(true);
                break;
            case R.id.btnFragmentLikes:
                fragmentId = R.id.fragment_likes;
                mBtnLikes.setSelected(true);
                break;
            case R.id.btnFragmentMutual:
                fragmentId = R.id.fragment_mutual;
                mBtnMutual.setSelected(true);
                break;
            case R.id.btnFragmentDialogs:
                fragmentId = R.id.fragment_dialogs;
                mBtnDialogs.setSelected(true);
                break;
            case R.id.btnFragmentTops:
                fragmentId = R.id.fragment_tops;
                mBtnTops.setSelected(true);
                break;
            case R.id.btnFragmentSettings:
                fragmentId = R.id.fragment_settings;
                mBtnSettings.setSelected(true);
                break; 
            default:
                break;
        }
        
        if(mFragmentMenuListener != null)
          mFragmentMenuListener.onMenuClick(fragmentId);
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
    
    public void setVisibility(int visibility) {
        mRootLayout.setVisibility(visibility);        
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
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
