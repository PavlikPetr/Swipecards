package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.ProfileNewFragment;

import java.util.ArrayList;

public class ProfileActionsControl extends RelativeLayout {

    private CheckBox mOpenActionButton;
    private ViewGroup mActionButtonsLayout;
    private ArrayList<ImageButton> mActionButtons = new ArrayList<ImageButton>();
    private ImageView mBackShadow;

    public ProfileActionsControl(Context context) {
        super(context,null);
    }

    public ProfileActionsControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.control_profile_actions, this, true);

        mBackShadow = (ImageView) findViewById(R.id.ivShadow);
        mOpenActionButton = (CheckBox) findViewById(R.id.cbOpenActions);
        mActionButtonsLayout = (ViewGroup) findViewById(R.id.loActionButtons);
        for (int i = 0; i < mActionButtonsLayout.getChildCount();i++) {
            if (mActionButtonsLayout.getChildAt(i) instanceof ImageButton) {
                mActionButtons.add((ImageButton)mActionButtonsLayout.getChildAt(i));
            }
        }
    }

    public void setType(int type) {
        switch (type) {
            case ProfileNewFragment.TYPE_MY_PROFILE:
                initMyProfileMode();
                break;
            case ProfileNewFragment.TYPE_USER_PROFILE:
                initUserProfileMode();
                break;
            default:
                this.setVisibility(View.GONE);
                break;
        }
    }

    public void setActionListener(int actionButtonIndex, OnClickListener listener) {
        if (actionButtonIndex < mActionButtons.size()) {
            mActionButtons.get(actionButtonIndex).setOnClickListener(listener);
        }
    }

    private void initMyProfileMode() {
        mOpenActionButton.setBackgroundResource(R.drawable.btn_action_rocket_selector);
        mOpenActionButton.setOnCheckedChangeListener(null);
        mActionButtonsLayout.setVisibility(View.GONE);
        mBackShadow.setVisibility(View.GONE);
    }

    private void initUserProfileMode() {
        mOpenActionButton.setBackgroundResource(R.drawable.btn_action_face_selector);
        mActionButtonsLayout.setVisibility(View.VISIBLE);
        mBackShadow.setVisibility(View.GONE);
        for (ImageButton btn : mActionButtons) {
            btn.setVisibility(View.GONE);
        }
        mOpenActionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < mActionButtons.size(); i++) {
                    ImageButton btn = mActionButtons.get(i);
                    btn.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    mBackShadow.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
            }
        });
    }
}
