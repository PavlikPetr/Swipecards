package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.utils.NavigationBarController;

public class ActionBar {

    private ViewGroup actionView;
    private ImageButton mNavigationHome;
    private ImageButton mNavigationBack;
    private TextView mTitle;
    private TextView mSubTitle;
    private ImageButton mSettingsButton;
    private Button mEditButton;
    private ImageButton mProfileButton;

    private NavigationBarController mNavBarController;

    public ActionBar(ViewGroup actionView) {
        this.actionView = actionView;
        mNavBarController = new NavigationBarController(actionView);
        initViews();
    }

    private void initViews() {
        mNavigationBack = (ImageButton) actionView.findViewById(R.id.btnNavigationBack);
        mNavigationHome = (ImageButton) actionView.findViewById(R.id.btnNavigationHome);
        mTitle = (TextView) actionView.findViewById(R.id.tvNavigationTitle);
        mSubTitle = (TextView) actionView.findViewById(R.id.tvNavigationSubtitle);
        mSettingsButton = (ImageButton) actionView.findViewById(R.id.btnNavigationSettingsBar);
        mEditButton = (Button) actionView.findViewById(R.id.btnNavigationRightWithText);
        mProfileButton = (ImageButton) actionView.findViewById(R.id.btnNavigationProfileBar);
    }

    public void refreshNotificators() {
        if (mNavBarController != null) {
            mNavBarController.refreshNotificators();
        }
    }

    public void showHomeButton(View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.GONE);
        mNavigationHome.setVisibility(View.VISIBLE);
        mNavigationHome.setOnClickListener(listener);
    }

    public void showBackButton(View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.VISIBLE);
        mNavigationHome.setVisibility(View.GONE);
        mNavigationHome.setOnClickListener(listener);
    }

    public  void showEditButton(View.OnClickListener listener) {
        mEditButton.setVisibility(View.VISIBLE);
        mEditButton.setOnClickListener(listener);

        mSettingsButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);
    }

    public void showSettingsButton(View.OnClickListener listener) {
        mEditButton.setVisibility(View.GONE);

        mSettingsButton.setVisibility(View.VISIBLE);
        mSettingsButton.setOnClickListener(listener);

        mProfileButton.setVisibility(View.GONE);
    }

    public void setTitleText(String text) {
        mTitle.setText(text);
    }

    public void setSubTitleText(String text) {
        mSubTitle.setVisibility(View.VISIBLE);
        mSubTitle.setText(text);
    }

    public void showProfileButton(View.OnClickListener listener) {
        mEditButton.setVisibility(View.GONE);
        mSettingsButton.setVisibility(View.GONE);

        mProfileButton.setVisibility(View.GONE);
        mProfileButton.setOnClickListener(listener);
    }
}
