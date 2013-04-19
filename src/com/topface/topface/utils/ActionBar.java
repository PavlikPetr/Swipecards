package com.topface.topface.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.views.ImageViewRemote;

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
    private ProgressBar mRightProgressBar;
    private ImageViewRemote mProfileAvatar;
    private CheckBox checkBox;

    private NavigationBarController mNavBarController;
    private ImageButton mSendButton;

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
        mRightProgressBar = (ProgressBar) actionView.findViewById(R.id.prsNavigationRight);
        mProfileAvatar = (ImageViewRemote) actionView.findViewById(R.id.btnNavigationBarAvatar);
        mSendButton = (ImageButton) actionView.findViewById(R.id.btnNavigationSend);
        checkBox = (CheckBox) actionView.findViewById(R.id.btnNavigationCheckbox);
    }

    public void refreshNotificators() {
        if (mNavBarController != null) {
            if (mNavigationHome.getVisibility() == View.VISIBLE) {
                mNavBarController.refreshNotificators();
            }
        }
    }

    public void showProfileAvatar() {
        showProfileAvatar(null, null);
    }

    public void showProfileAvatar(final Photo profilePhoto, View.OnClickListener listener) {
        hideRightBarPart();
        mProfileAvatar.setVisibility(View.VISIBLE);
        mProfileAvatar.setPhoto(profilePhoto);
        mProfileAvatar.setOnClickListener(listener);
    }

    public void showHomeButton(final View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.GONE);
        mNavigationHome.setVisibility(View.VISIBLE);
        mNavigationHome.setSelected(false);
        mNavigationHome.setOnClickListener(listener);
    }

    public void activateHomeButton(boolean activate) {
        mNavigationHome.setSelected(activate);

    }

    public void showBackButton(View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.VISIBLE);
        mNavigationHome.setVisibility(View.GONE);
        mNavigationBack.setOnClickListener(listener);
    }

    public void showEditButton(View.OnClickListener listener) {
        mEditButton.setVisibility(View.VISIBLE);
        mEditButton.setOnClickListener(listener);

        mSettingsButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);
        mSendButton.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
    }

    public void showSettingsButton(final View.OnClickListener listener, final boolean changeSelection) {
        mEditButton.setVisibility(View.GONE);

        mSettingsButton.setVisibility(View.VISIBLE);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (changeSelection) {
                    if (mSettingsButton.isSelected()) {
                        mSettingsButton.setSelected(false);
                    } else {
                        mSettingsButton.setSelected(true);
                    }
                }
                listener.onClick(view);
            }

        });

        mProfileButton.setVisibility(View.GONE);
        mUserActionsControl.setVisibility(View.GONE);
        mSendButton.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
    }

    public void showSendButton(final View.OnClickListener listener) {
        mEditButton.setVisibility(View.GONE);
        mSettingsButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);
        mUserActionsControl.setVisibility(View.GONE);
        mSendButton.setVisibility(View.VISIBLE);
        mSendButton.setOnClickListener(listener);
        checkBox.setVisibility(View.GONE);
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
        mSendButton.setVisibility(View.GONE);
        mUserActionsControl.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
    }

    public void showUserActionsButton(final View.OnClickListener nonActiveListener, final View.OnClickListener activeListener) {
        mEditButton.setVisibility(View.GONE);
        mSettingsButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);

        mUserActionsControl.setVisibility(View.VISIBLE);
        mUserActionsControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUserActionsControl.isSelected()) {
                    mUserActionsControl.setSelected(false);
                    activeListener.onClick(view);
                } else {
                    mUserActionsControl.setSelected(true);
                    nonActiveListener.onClick(view);
                }
            }
        });
        mSendButton.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
    }

    public void showCheckBox(View.OnClickListener listener, boolean isChecked) {
        hideRightButtons();
        checkBox.setVisibility(View.VISIBLE);
        checkBox.setOnClickListener(listener);
        checkBox.setChecked(true);
    }

    private void hideRightButtons() {
        mEditButton.setVisibility(View.GONE);
        mSettingsButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);
        mUserActionsControl.setVisibility(View.GONE);
        mSendButton.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
    }

    public void hideUserActionButton() {
        mUserActionsControl.setVisibility(View.GONE);
    }

    public void activateEditButton() {
        if (mEditButton.isSelected()) {
            mEditButton.setSelected(false);
        } else {
            mEditButton.setSelected(true);
        }
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

    public ProgressBar getRightProgressBar() {
        return mRightProgressBar;
    }

    private void hideRightBarPart() {
        mSettingsButton.setVisibility(View.GONE);
        mEditButton.setVisibility(View.GONE);
        mProfileButton.setVisibility(View.GONE);
        mUserActionsControl.setVisibility(View.GONE);
        mRightProgressBar.setVisibility(View.GONE);
        mProfileAvatar.setVisibility(View.GONE);
    }
}
