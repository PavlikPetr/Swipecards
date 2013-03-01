package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.NavigationBarController;

public class ActionBar {

    private ViewGroup actionView;
    private ImageButton mNavigationHome;
    private ImageButton mNavigationBack;
    private TextView mTitle;
    private TextView mSubTitle;
    private ImageButton mSettingsButton;
    private ImageButton mEditButton;
    private ImageButton mProfileButton;
    private ImageButton mUserActionsControl;

    private NavigationBarController mNavBarController;

    public ActionBar(View actionView) {
        this.actionView = (ViewGroup) actionView.findViewById(R.id.loNavigationBar);
        mNavBarController = new NavigationBarController(this.actionView);
        initViews();
    }

    private void initViews() {
        mNavigationBack = (ImageButton) actionView.findViewById(R.id.btnNavigationBack);
        mNavigationHome = (ImageButton) actionView.findViewById(R.id.btnNavigationHome);
        mTitle = (TextView) actionView.findViewById(R.id.tvNavigationTitle);
        mSubTitle = (TextView) actionView.findViewById(R.id.tvNavigationSubtitle);
        mSettingsButton = (ImageButton) actionView.findViewById(R.id.btnNavigationSettingsBar);
        mEditButton = (ImageButton) actionView.findViewById(R.id.btnEdit);
        mProfileButton = (ImageButton) actionView.findViewById(R.id.btnNavigationProfileBar);
        mUserActionsControl = (ImageButton) actionView.findViewById(R.id.btnUserProfActions);
    }

    public void refreshNotificators() {
        if (mNavBarController != null) {
            mNavBarController.refreshNotificators();
        }
    }

    public void showHomeButton(final View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.GONE);
        mNavigationHome.setVisibility(View.VISIBLE);
        mNavigationHome.setSelected(false);
        mNavigationHome.setOnClickListener(listener);
    }

    public void activateHomeButton() {
        if (mNavigationHome.isSelected()) {
            mNavigationHome.setSelected(false);
        } else {
            mNavigationHome.setSelected(true);
        }
    }

    public void showBackButton(View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.VISIBLE);
        mNavigationHome.setVisibility(View.GONE);
        mNavigationBack.setOnClickListener(listener);
    }

    public  void showEditButton(View.OnClickListener listener) {
        mEditButton.setVisibility(View.VISIBLE);
        mEditButton.setOnClickListener(listener);

        mSettingsButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);
    }

    public void showSettingsButton(final View.OnClickListener listener) {
        mEditButton.setVisibility(View.GONE);

        mSettingsButton.setVisibility(View.VISIBLE);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSettingsButton.isSelected()) {
                    mSettingsButton.setSelected(false);
                } else {
                    mSettingsButton.setSelected(true);
                }
                listener.onClick(view);
            }

        });

        mProfileButton.setVisibility(View.GONE);
        mUserActionsControl.setVisibility(View.GONE);
    }

    public void setTitleText(String text) {
        mTitle.setText(text);
    }

    public void setSubTitleText(String text) {
        mSubTitle.setVisibility(View.VISIBLE);
        mSubTitle.setText(text);
    }

    public void showProfileButton(View.OnClickListener listener, int userSex) {
        mEditButton.setVisibility(View.GONE);
        mSettingsButton.setVisibility(View.GONE);

        mProfileButton.setVisibility(View.VISIBLE);
        switch (userSex) {
            case Static.BOY:
                mProfileButton.setImageResource(R.drawable.navigation_male_profile_selector);
                break;
            case Static.GIRL:
                mProfileButton.setImageResource(R.drawable.navigation_female_profile_selector);
                break;
        }
        mProfileButton.setOnClickListener(listener);

        mUserActionsControl.setVisibility(View.GONE);
    }

    public void showUserActionsButton(final View.OnClickListener nonActiveListener, final View.OnClickListener activeListener) {
        mEditButton.setVisibility(View.GONE);
        mSettingsButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);

        mUserActionsControl.setVisibility(View.VISIBLE);
        mUserActionsControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mUserActionsControl.isSelected()) {
                    mUserActionsControl.setSelected(false);
                    activeListener.onClick(view);
                } else {
                    mUserActionsControl.setSelected(true);
                    nonActiveListener.onClick(view);
                }
            }
        });
    }

    public void disableActionsButton(boolean disabled) {
        mUserActionsControl.setEnabled(!disabled);
    }

    public void hide() {
        actionView.setVisibility(View.GONE);
    }

    public void show() {
        actionView.setVisibility(View.VISIBLE);
    }

    public int getHeight() {
        return actionView.getHeight();
    }
}
