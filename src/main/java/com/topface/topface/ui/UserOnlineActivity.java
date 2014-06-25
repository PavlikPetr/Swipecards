package com.topface.topface.ui;

import android.view.View;

import com.topface.topface.R;

/**
 * Activity showing whether user online or not
 */
public class UserOnlineActivity extends CheckAuthActivity implements IUserOnlineListener {

    private View mOnlineIcon;

    @Override
    protected void initCustomActionBarView(View mCustomView) {
        mOnlineIcon = mCustomView.findViewById(R.id.online);
    }

    @Override
    protected int getActionBarCustomViewResId() {
        return R.layout.actionbar_container_title_view;
    }

    @Override
    public void setUserOnline(boolean online) {
        if (mOnlineIcon != null) {
            mOnlineIcon.setVisibility(online ? View.VISIBLE : View.GONE);
        }
    }
}
