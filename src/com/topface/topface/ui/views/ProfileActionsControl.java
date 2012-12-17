package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.ProfileNewFragment;

import java.util.ArrayList;

public class ProfileActionsControl extends RelativeLayout {

    private int mType;
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
        mType = type;
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

    public void setOnClickListener(OnClickListener listener) {
        for (ImageButton btn : mActionButtons) {
            btn.setOnClickListener(listener);
        }
    }

    public void setOnCheckChangedListener(CompoundButton.OnCheckedChangeListener listener) {
        if (mType == ProfileNewFragment.TYPE_MY_PROFILE) {
            mOpenActionButton.setOnCheckedChangeListener(listener);
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
                if (isChecked) {
                    mActionButtonsLayout.setVisibility(View.VISIBLE);
                    for (int i = 0; i < mActionButtons.size(); i++) {
                        ImageButton btn = mActionButtons.get(i);
                        btn.startAnimation(getActionButtonInAnimation(i, mActionButtons.size()));
                        btn.setVisibility(View.VISIBLE);
                    }
                    mBackShadow.startAnimation(getShadowInAnimation());
                    mBackShadow.setVisibility(View.VISIBLE);
                } else {
                    for (int i = 0; i < mActionButtons.size(); i++) {
                        final ImageButton btn = mActionButtons.get(i);
                        btn.startAnimation(getActionButtonOutAnimation(i, mActionButtons.size(), new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) { }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                btn.setVisibility(View.INVISIBLE);
                                boolean hasVisibleBtns = false;
                                for (ImageButton ib: mActionButtons) {
                                    if (ib.getVisibility() == View.VISIBLE)
                                        hasVisibleBtns = true;
                                }

                                if (!hasVisibleBtns) {
                                    mActionButtonsLayout.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) { }
                        }));

                    }
                    mBackShadow.startAnimation(getShadowOutAnimation(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mBackShadow.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    }));

                }
            }
        });
    }

    private AnimationSet getActionButtonInAnimation(int index,int size) {
        //AnimationSet result = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.action_btn_in);
        AnimationSet result = new AnimationSet(true);
        result.setInterpolator(getContext(), android.R.anim.anticipate_overshoot_interpolator);

        RotateAnimation rotate = new RotateAnimation(
                210*(size-index),
                0,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setDuration(300*(index+1));
        result.addAnimation(rotate);

        TranslateAnimation translate = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, //fromXType
                1.0f*(size-index),          //fromXValue
                Animation.RELATIVE_TO_SELF, //toXType
                0.0f,                       //toXValue
                Animation.RELATIVE_TO_SELF, //fromYType
                0.0f,                       //fromYValue
                Animation.RELATIVE_TO_SELF, //toYType
                0.0f);
        translate.setDuration(300 * (index + 1));
        result.addAnimation(translate);

        return result;
    }

    private Animation getShadowInAnimation() {
        Animation result = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, //fromXType
                0.0f,                       //fromXValue
                Animation.RELATIVE_TO_SELF, //toXType
                0.0f,                       //toXValue
                Animation.RELATIVE_TO_SELF, //fromYType
                1.0f,                      //fromYValue
                Animation.RELATIVE_TO_SELF, //toYType
                0.0f);
        result.setDuration(500);
        return result;
    }

    private AnimationSet getActionButtonOutAnimation(int index,int size,Animation.AnimationListener listener) {
        //AnimationSet result = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.action_btn_in);
        AnimationSet result = new AnimationSet(true);
        result.setInterpolator(getContext(), android.R.anim.anticipate_overshoot_interpolator);

        RotateAnimation rotate = new RotateAnimation(
                0,
                210*(index+1),
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setDuration(300*(size-index));
        result.addAnimation(rotate);

        TranslateAnimation translate = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, //fromXType
                0.0f,          //fromXValue
                Animation.RELATIVE_TO_SELF, //toXType
                1.0f*(size-index),                       //toXValue
                Animation.RELATIVE_TO_SELF, //fromYType
                0.0f,                       //fromYValue
                Animation.RELATIVE_TO_SELF, //toYType
                0.0f);
        translate.setDuration(300*(size-index));
        result.addAnimation(translate);

        result.setAnimationListener(listener);
        return result;
    }

    private Animation getShadowOutAnimation(Animation.AnimationListener listener) {
        Animation result = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, //fromXType
                0.0f,                       //fromXValue
                Animation.RELATIVE_TO_SELF, //toXType
                0.0f,                       //toXValue
                Animation.RELATIVE_TO_SELF, //fromYType
                0.0f,                      //fromYValue
                Animation.RELATIVE_TO_SELF, //toYType
                1.0f);
        result.setDuration(1000);
        result.setAnimationListener(listener);
        return result;
    }
}
