package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.views.ImageViewRemote;

public class ActionBar {

    private ViewGroup actionView;
    private ImageView mNavigationHome;
    private ImageView mNavigationBack;
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
    private RelativeLayout leftContainer;

    public ActionBar(Context context, View actionView) {
        this.actionView = (ViewGroup) actionView.findViewById(R.id.loNavigationBar);
        initShadow(context);
        this.actionView.setVisibility(View.VISIBLE);
        mNavBarController = new NavigationBarController(this.actionView);
        initViews();
    }

    private void initShadow(Context context) {
        if (this.actionView.getParent() instanceof RelativeLayout) {
            RelativeLayout parent = (RelativeLayout) this.actionView.getParent();
            View shadow = new ImageView(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, R.id.loNavigationBar);
            shadow.setLayoutParams(params);
            shadow.setBackgroundResource(R.drawable.im_bar_header_shadow);
            shadow.setTag(context.getString(R.string.tag_header_shadow));
            parent.addView(shadow);
        }
    }

    private void initViews() {

        mNavigationBack = (ImageView) actionView.findViewById(R.id.btnNavigationBack);
        mNavigationHome = (ImageView) actionView.findViewById(R.id.btnNavigationHome);
        mTitle = (TextView) actionView.findViewById(R.id.tvNavigationTitle);
        mSubTitle = (TextView) actionView.findViewById(R.id.tvNavigationSubtitle);
        mSettingsButton = (ImageButton) actionView.findViewById(R.id.btnNavigationSettingsBar);
        mEditButton = (ImageButton) actionView.findViewById(R.id.btnEdit);
        mProfileButton = (ImageButton) actionView.findViewById(R.id.btnNavigationProfileBar);
        mUserActionsControl = (ImageButton) actionView.findViewById(R.id.btnUserProfActions);
        mRightProgressBar = (ProgressBar) actionView.findViewById(R.id.prsNavigationRight);
        mProfileAvatar = (ImageViewRemote) actionView.findViewById(R.id.btnNavigationBarAvatar);
//        mProfileAvatar.setBackgroundResource(R.drawable.feed_photo_selector);
        mSendButton = (ImageButton) actionView.findViewById(R.id.btnNavigationSend);
        checkBox = (CheckBox) actionView.findViewById(R.id.btnNavigationCheckbox);
        leftContainer = (RelativeLayout) actionView.findViewById(R.id.leftButtonContainer);
    }

    public void refreshNotificators() {
        if (mNavBarController != null) {
            if (mNavigationHome.getVisibility() == View.VISIBLE) {
                mNavBarController.refreshNotificators();
            }
        }
    }

    public void showProfileAvatar(final Photo profilePhoto, View.OnClickListener listener) {
        hideRightBarPart();
        mProfileAvatar.setVisibility(View.VISIBLE);
//        mProfileAvatar.setPhoto(profilePhoto);
        mProfileAvatar.setOnClickListener(listener);
    }

    public void showProfileAvatar(final int profilePhotorResource, View.OnClickListener listener) {
        hideRightBarPart();
        mProfileAvatar.setVisibility(View.VISIBLE);
//        mProfileAvatar.setImageResource(profilePhotorResource);
        mProfileAvatar.setOnClickListener(listener);
    }

    public void showHomeButton(final View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.GONE);
        mNavigationHome.setVisibility(View.VISIBLE);
        mNavigationHome.setSelected(false);
        leftContainer.setOnClickListener(listener);
    }

    public void activateHomeButton(boolean activate) {
        mNavigationHome.setSelected(activate);

    }

    public void showBackButton(final View.OnClickListener listener) {
        mNavigationBack.setVisibility(View.VISIBLE);
        mNavigationHome.setVisibility(View.GONE);
        leftContainer.setOnClickListener(listener);
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


    public void setOnlineIcon(boolean online) {
        mTitle.setCompoundDrawablesWithIntrinsicBounds(online ? R.drawable.ico_online : 0, 0, 0, 0);
    }

    public void setSubTitleText(String text) {
        mSubTitle.setVisibility(View.VISIBLE);
        mSubTitle.setText(text);
    }

    public void showUserActionsButton(final View.OnClickListener nonActiveListener, final View.OnClickListener activeListener, final Photo profilePhotoResource) {
        hideRightBarPart();
        mProfileAvatar.setVisibility(View.VISIBLE);

        mProfileAvatar.setPhoto(profilePhotoResource);
        userActionsOnClickListener = new UserActionsOnClickListener(activeListener, nonActiveListener);
        mProfileAvatar.setOnClickListener(userActionsOnClickListener);
        mSendButton.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
    }

    private UserActionsOnClickListener userActionsOnClickListener;

    private class UserActionsOnClickListener implements View.OnClickListener {
        private boolean selected = false;
        private View.OnClickListener active, nonactive;

        public UserActionsOnClickListener(View.OnClickListener active, View.OnClickListener nonActive) {
            this.active = active;
            this.nonactive = nonActive;
        }

        @Override
        public void onClick(View view) {
            if (selected) {
                selected = false;
                active.onClick(view);
            } else {
                selected = true;
                nonactive.onClick(view);
            }
        }

        public void setState(boolean state) {
            selected = state;
        }
    };

    public void setUserActionsControlActive(boolean state) {
        if (userActionsOnClickListener != null) {
            userActionsOnClickListener.setState(state);
        }
    }

    public void showUserActionsButton(final View.OnClickListener nonActiveListener, final View.OnClickListener activeListener) {

        hideRightBarPart();
        mUserActionsControl.setVisibility(View.VISIBLE);

        mUserActionsControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
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


    public void showCheckBox(View.OnClickListener listener) {
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
        if (mUserActionsControl.getVisibility() == View.VISIBLE) {
            mUserActionsControl.setEnabled(!disabled);
        } else if (mProfileAvatar.getVisibility() == View.VISIBLE) {
            mProfileAvatar.setEnabled(!disabled);
        }
    }

    public void setSendButtonEnabled(boolean enabled) {
        mSendButton.setEnabled(enabled);
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
